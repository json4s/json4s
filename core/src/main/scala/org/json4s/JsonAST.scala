/*
 * Copyright 2009-2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s
object JsonAST {
  import scala.text.{Document, DocText}
  import scala.text.Document._
  import mojolly.inflector.InflectorImports._

  /** Concatenates a sequence of <code>JValue</code>s.
   * <p>
   * Example:<pre>
   * concat(JInt(1), JInt(2)) == JArray(List(JInt(1), JInt(2)))
   * </pre>
   */
  def concat(xs: JValue*) = xs.foldLeft(JNothing: JValue)(_ ++ _)

  object JValue extends Merge.Mergeable

  /**
   * Data type for JSON AST.
   */
  sealed abstract class JValue extends Diff.Diffable {
    type Values

    /** XPath-like expression to query JSON fields by name. Matches only fields on
     * next level.
     * <p>
     * Example:<pre>
     * json \ "name"
     * </pre>
     */
    def \(nameToFind: String): JValue =
      findDirectByName(List(this), nameToFind) match {
        case Nil => JNothing
        case x :: Nil => x
        case x => JArray(x)
      }

    private def findDirectByName(xs: List[JValue], name: String): List[JValue] = xs.flatMap {
      case JObject(l) => l.filter {
        case (n, _) if n == name => true
        case _ => false
      } map (_._2)
      case JArray(l) => findDirectByName(l, name)
      case _ => Nil
    }

    private def findDirect(xs: List[JValue], p: JValue => Boolean): List[JValue] = xs.flatMap {
      case JObject(l) => l.filter {
        case (n, x) if p(x) => true
        case _ => false
      } map (_._2)
      case JArray(l) => findDirect(l, p)
      case x if p(x) => x :: Nil
      case _ => Nil
    }

    /** XPath-like expression to query JSON fields by name. Returns all matching fields.
     * <p>
     * Example:<pre>
     * json \\ "name"
     * </pre>
     */
    def \\(nameToFind: String): JValue = {
      def find(json: JValue): List[JField] = json match {
        case JObject(l) => l.foldLeft(List[JField]()) {
          case (a, (name, value)) =>
            if (name == nameToFind) a ::: List((name, value)) ::: find(value) else a ::: find(value)
        }
        case JArray(l) => l.foldLeft(List[JField]())((a, json) => a ::: find(json))
        case _ => Nil
      }
      find(this) match {
        case (_, x) :: Nil => x
        case xs => JObject(xs)
      }
    }

    /** XPath-like expression to query JSON fields by type. Matches only fields on
     * next level.
     * <p>
     * Example:<pre>
     * json \ classOf[JInt]
     * </pre>
     */
    def \[A <: JValue](clazz: Class[A]): List[A#Values] =
      findDirect(children, typePredicate(clazz) _).asInstanceOf[List[A]] map { _.values }

    /** XPath-like expression to query JSON fields by type. Returns all matching fields.
     * <p>
     * Example:<pre>
     * json \\ classOf[JInt]
     * </pre>
     */
    def \\[A <: JValue](clazz: Class[A]): List[A#Values] =
      (this filter typePredicate(clazz) _).asInstanceOf[List[A]] map { _.values }

    private def typePredicate[A <: JValue](clazz: Class[A])(json: JValue) = json match {
      case x if x.getClass == clazz => true
      case _ => false
    }

    /** Return nth element from JSON.
     * Meaningful only to JArray, JObject and JField. Returns JNothing for other types.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil)(1) == JInt(2)
     * </pre>
     */
    def apply(i: Int): JValue = JNothing

    /** Return unboxed values from JSON
     * <p>
     * Example:<pre>
     * JObject(JField("name", JString("joe")) :: Nil).values == Map("name" -> "joe")
     * </pre>
     */
    def values: Values

    /** Return direct child elements.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil).children == List(JInt(1), JInt(2))
     * </pre>
     */
    def children: List[JValue] = this match {
      case JObject(l) => l map (_._2)
      case JArray(l) => l
      case _ => Nil
    }

    /** Return a combined value by folding over JSON by applying a function <code>f</code>
     * for each element. The initial value is <code>z</code>.
     */
    def fold[A](z: A)(f: (A, JValue) => A): A = {
      def rec(acc: A, v: JValue) = {
        val newAcc = f(acc, v)
        v match {
          case JObject(l) => l.foldLeft(newAcc) { case (a, (name, value)) => value.fold(a)(f) }
          case JArray(l) => l.foldLeft(newAcc)((a, e) => e.fold(a)(f))
          case _ => newAcc
        }
      }
      rec(z, this)
    }

    /** Return a combined value by folding over JSON by applying a function <code>f</code>
     * for each field. The initial value is <code>z</code>.
     */
    def foldField[A](z: A)(f: (A, JField) => A): A = {
      def rec(acc: A, v: JValue) = {
        v match {
          case JObject(l) => l.foldLeft(acc) {
            case (a, field@(name, value)) => value.foldField(f(a, field))(f)
          }
          case JArray(l) => l.foldLeft(acc)((a, e) => e.foldField(a)(f))
          case _ => acc
        }
      }
      rec(z, this)
    }

    /** Return a new JValue resulting from applying the given function <code>f</code>
     * to each value in JSON.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) map {
     *   case JInt(x) => JInt(x+1)
     *   case x => x
     * }
     * </pre>
     */
    def map(f: JValue => JValue): JValue = {
      def rec(v: JValue): JValue = v match {
        case JObject(l) => f(JObject(l.map { case (n, va) => (n, rec(va)) }))
        case JArray(l) => f(JArray(l.map(rec)))
        case x => f(x)
      }
      rec(this)
    }

    /** Return a new JValue resulting from applying the given function <code>f</code>
     * to each field in JSON.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) map {
     *   case ("age", JInt(x)) => ("age", JInt(x+1))
     *   case x => x
     * }
     * </pre>
     */
    def mapField(f: JField => JField): JValue = {
      def rec(v: JValue): JValue = v match {
        case JObject(l) => JObject(l.map { case (n, va) => f(n, rec(va)) })
        case JArray(l) => JArray(l.map(rec))
        case x => x
      }
      rec(this)
    }

    /** Return a new JValue resulting from applying the given partial function <code>f</code>
     * to each field in JSON.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) transformField {
     *   case ("age", JInt(x)) => ("age", JInt(x+1))
     * }
     * </pre>
     */
    def transformField(f: PartialFunction[JField, JField]): JValue = mapField { x =>
      if (f.isDefinedAt(x)) f(x) else x
    }

    /** Return a new JValue resulting from applying the given partial function <code>f</code>
     * to each value in JSON.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) transform { case JInt(x) => JInt(x+1) }
     * </pre>
     */
    def transform(f: PartialFunction[JValue, JValue]): JValue = map { x =>
      if (f.isDefinedAt(x)) f(x) else x
    }

    /** Return a new JValue resulting from replacing the value at the specified field
     * path with the replacement value provided. This has no effect if the path is empty
     * or if the value is not a JObject instance.
     * <p>
     * Example:<pre>
     * JObject(List(JField("foo", JObject(List(JField("bar", JInt(1))))))).replace("foo" :: "bar" :: Nil, JString("baz"))
     * // returns JObject(List(JField("foo", JObject(List(JField("bar", JString("baz")))))))
     * </pre>
     */
    def replace(l: List[String], replacement: JValue): JValue = {
      def rep(l: List[String], in: JValue): JValue = {
        l match {
          case x :: xs => in match {
            case JObject(fields) => JObject(
                fields.map {
                  case JField(`x`, value) => JField(x, if (xs == Nil) replacement else rep(xs, value))
                  case field => field
                }
              )
            case other => other
          }

          case Nil => in
        }
      }

      rep(l, this)
    }

    /** Return the first field from JSON which matches the given predicate.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(2))) findField { case (n, v) => n == "age" }
     * </pre>
     */
    def findField(p: JField => Boolean): Option[JField] = {
      def find(json: JValue): Option[JField] = json match {
        case JObject(fs) if (fs find p).isDefined => return fs find p
        case JObject(fs) => fs.flatMap { case (n, v) => find(v) }.headOption
        case JArray(l) => l.flatMap(find _).headOption
        case _ => None
      }
      find(this)
    }

    /** Return the first element from JSON which matches the given predicate.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) find { _ == JInt(2) } == Some(JInt(2))
     * </pre>
     */
    def find(p: JValue => Boolean): Option[JValue] = {
      def find(json: JValue): Option[JValue] = {
        if (p(json)) return Some(json)
        json match {
          case JObject(fs) => fs.flatMap { case (n, v) => find(v) }.headOption
          case JArray(l) => l.flatMap(find _).headOption
          case _ => None
        }
      }
      find(this)
    }

    /** Return a List of all fields which matches the given predicate.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) filterField {
     *   case ("age", JInt(x)) if x > 18 => true
     *   case _          => false
     * }
     * </pre>
     */
    def filterField(p: JField => Boolean): List[JField] =
      foldField(List[JField]())((acc, e) => if (p(e)) e :: acc else acc).reverse

    /** Return a List of all values which matches the given predicate.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) filter { case JInt(x) => x > 1; case _ => false }
     * </pre>
     */
    def filter(p: JValue => Boolean): List[JValue] =
      fold(List[JValue]())((acc, e) => if (p(e)) e :: acc else acc).reverse

    /** Concatenate with another JSON.
     * This is a concatenation monoid: (JValue, ++, JNothing)
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) ++ JArray(JInt(3) :: Nil) ==
     * JArray(List(JInt(1), JInt(2), JInt(3)))
     * </pre>
     */
    def ++(other: JValue) = {
      def append(value1: JValue, value2: JValue): JValue = (value1, value2) match {
        case (JNothing, x) => x
        case (x, JNothing) => x
        case (JArray(xs), JArray(ys)) => JArray(xs ::: ys)
        case (JArray(xs), v: JValue) => JArray(xs ::: List(v))
        case (v: JValue, JArray(xs)) => JArray(v :: xs)
        case (x, y) => JArray(x :: y :: Nil)
      }
      append(this, other)
    }

    /** Return a JSON where all fields matching the given predicate are removed.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) removeField {
     *   case ("age", _) => true
     *   case _          => false
     * }
     * </pre>
     */
    def removeField(p: JField => Boolean): JValue = this mapField {
      case x if p(x) => (x._1, JNothing)
      case x => x
    }

    /** Return a JSON where all values matching the given predicate are removed.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: JNull :: Nil) remove { _ == JNull }
     * </pre>
     */
    def remove(p: JValue => Boolean): JValue = this map {
      case x if p(x) => JNothing
      case x => x
    }

    /** Extract a value from a JSON.
     * <p>
     * Value can be:
     * <ul>
     *   <li>case class</li>
     *   <li>primitive (String, Boolean, Date, etc.)</li>
     *   <li>supported collection type (List, Seq, Map[String, _], Set)</li>
     *   <li>any type which has a configured custom deserializer</li>
     * </ul>
     * <p>
     * Example:<pre>
     * case class Person(name: String)
     * JObject(JField("name", JString("joe")) :: Nil).extract[Person] == Person("joe")
     * </pre>
     */
    def extract[A](implicit formats: Formats, mf: scala.reflect.Manifest[A]): A =
      Extraction.extract(this)(formats, mf)

    /** Extract a value from a JSON.
     * <p>
     * Value can be:
     * <ul>
     *   <li>case class</li>
     *   <li>primitive (String, Boolean, Date, etc.)</li>
     *   <li>supported collection type (List, Seq, Map[String, _], Set)</li>
     *   <li>any type which has a configured custom deserializer</li>
     * </ul>
     * <p>
     * Example:<pre>
     * case class Person(name: String)
     * JObject(JField("name", JString("joe")) :: Nil).extractOpt[Person] == Some(Person("joe"))
     * </pre>
     */
    def extractOpt[A](implicit formats: Formats, mf: scala.reflect.Manifest[A]): Option[A] =
      Extraction.extractOpt(this)(formats, mf)

    /** Extract a value from a JSON using a default value.
     * <p>
     * Value can be:
     * <ul>
     *   <li>case class</li>
     *   <li>primitive (String, Boolean, Date, etc.)</li>
     *   <li>supported collection type (List, Seq, Map[String, _], Set)</li>
     *   <li>any type which has a configured custom deserializer</li>
     * </ul>
     * <p>
     * Example:<pre>
     * case class Person(name: String)
     * JNothing.extractOrElse(Person("joe")) == Person("joe")
     * </pre>
     */
    def extractOrElse[A](default: => A)(implicit formats: Formats, mf: scala.reflect.Manifest[A]): A =
      Extraction.extractOpt(this)(formats, mf).getOrElse(default)

    def as[A](implicit reader: Reader[A],  mf: Manifest[A]): A = reader.read(this)

    def asOption[A](implicit reader: Reader[A], mf: scala.reflect.Manifest[A]): Option[A] = try {
      toOpt map reader.read
    } catch { case _: Throwable => None }

    def getAsOrElse[A](default: => A)(implicit reader: Reader[A], mf: Manifest[A]): A =
      asOption(reader, mf) getOrElse default

    def camelizeKeys = rewriteJsonAST(camelize = true)
    def snakizeKeys = rewriteJsonAST(camelize = false)

    private def rewriteJsonAST(camelize: Boolean): JValue = {
      transformField {
        case JField(nm, x) if !nm.startsWith("_") ⇒ JField(if (camelize) nm.camelize else nm.underscore, x)
        case x                                    ⇒ x
      }
    }

    def noNulls = removeNulls(this)

    private[this] def removeNulls(initial: JValue): JValue = {
      initial match {
        case JArray(values) ⇒ JArray(values map removeNulls)
        case j: JObject     ⇒ removeNullsFromJObject(j)
        case _              ⇒ initial
      }
    }

    private[this] def removeNullsFromJObject(initial: JObject): JValue = {
      initial transformField {
        case JField(a, JNull) ⇒ JField(a, JNothing)
      }
    }

    def toOpt: Option[JValue] = this match {
      case JNothing => None
      case json     => Some(json)
    }
  }

  case object JNothing extends JValue {
    type Values = None.type
    def values = None
  }
  case object JNull extends JValue {
    type Values = Null
    def values = null
  }
  case class JString(s: String) extends JValue {
    type Values = String
    def values = s
  }
  case class JDouble(num: Double) extends JValue {
    type Values = Double
    def values = num
  }
  case class JDecimal(num: BigDecimal) extends JValue {
    type Values = BigDecimal
    def values = num
  }
  case class JInt(num: BigInt) extends JValue {
    type Values = BigInt
    def values = num
  }
  case class JBool(value: Boolean) extends JValue {
    type Values = Boolean
    def values = value
  }

  case class JObject(obj: List[JField]) extends JValue {
    type Values = Map[String, Any]
    def values = obj.map { case (n, v) => (n, v.values) } toMap

    override def equals(that: Any): Boolean = that match {
      case o: JObject => Set(obj.toArray: _*) == Set(o.obj.toArray: _*)
      case _ => false
    }
  }
  case object JObject {
    def apply(fs: JField*): JObject = JObject(fs.toList)
  }

  case class JArray(arr: List[JValue]) extends JValue {
    type Values = List[Any]
    def values = arr.map(_.values)
    override def apply(i: Int): JValue = arr(i)
  }

  type JField = (String, JValue)
  object JField {
    def apply(name: String, value: JValue) = (name, value)
    def unapply(f: JField): Option[(String, JValue)] = Some(f)
  }

  private[json4s] def quote(s: String): String = {
    val buf = new StringBuilder
    for (i <- 0 until s.length) {
      val c = s.charAt(i)
      buf.append(c match {
        case '"'  => "\\\""
        case '\\' => "\\\\"
        case '\b' => "\\b"
        case '\f' => "\\f"
        case '\n' => "\\n"
        case '\r' => "\\r"
        case '\t' => "\\t"
        case c if ((c >= '\u0000' && c < '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) => "\\u%04x".format(c: Int)
        case c => c
      })
    }
    buf.toString
  }
}

/** Basic implicit conversions from primitive types into JSON.
 * Example:<pre>
 * import org.json4s.Implicits._
 * JObject(JField("name", "joe") :: Nil) == JObject(JField("name", JString("joe")) :: Nil)
 * </pre>
 */
trait BigDecimalMode { self: Implicits =>
  implicit def double2jvalue(x: Double): JValue = JDecimal(x)
  implicit def float2jvalue(x: Float): JValue = JDecimal(x.toDouble)
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue = JDecimal(x)

}
object BigDecimalMode extends Implicits with BigDecimalMode
trait DoubleMode { self: Implicits =>
  implicit def double2jvalue(x: Double): JValue = JDouble(x)
  implicit def float2jvalue(x: Float): JValue = JDouble(x.toDouble)
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue = JDouble(x.doubleValue())

}
object DoubleMode extends Implicits with DoubleMode
trait Implicits {
  implicit def int2jvalue(x: Int) = JInt(x)
  implicit def long2jvalue(x: Long) = JInt(x)
  implicit def bigint2jvalue(x: BigInt) = JInt(x)
  implicit def double2jvalue(x: Double): JValue
  implicit def float2jvalue(x: Float): JValue
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue
  implicit def boolean2jvalue(x: Boolean) = JBool(x)
  implicit def string2jvalue(x: String) = JString(x)
}

/** A DSL to produce valid JSON.
 * Example:<pre>
 * import org.json4s.JsonDSL._
 * ("name", "joe") ~ ("age", 15) == JObject(JField("name",JString("joe")) :: JField("age",JInt(15)) :: Nil)
 * </pre>
 */
object JsonDSL extends JsonDSL with DoubleMode {
  object WithDouble extends JsonDSL with DoubleMode
  object WithBigDecimal extends JsonDSL with BigDecimalMode
}
trait JsonDSL extends Implicits {
  implicit def seq2jvalue[A <% JValue](s: Traversable[A]) =
    JArray(s.toList.map { a => val v: JValue = a; v })

  implicit def map2jvalue[A <% JValue](m: Map[String, A]) =
    JObject(m.toList.map { case (k, v) => JField(k, v) })

  implicit def option2jvalue[A <% JValue](opt: Option[A]): JValue = opt match {
    case Some(x) => x
    case None => JNothing
  }

  implicit def symbol2jvalue(x: Symbol) = JString(x.name)
  implicit def pair2jvalue[A <% JValue](t: (String, A)) = JObject(List(JField(t._1, t._2)))
  implicit def list2jvalue(l: List[JField]) = JObject(l)
  implicit def jobject2assoc(o: JObject) = new JsonListAssoc(o.obj)
  implicit def pair2Assoc[A <% JValue](t: (String, A)) = new JsonAssoc(t)

  class JsonAssoc[A <% JValue](left: (String, A)) {
    def ~[B <% JValue](right: (String, B)) = {
      val l: JValue = left._2
      val r: JValue = right._2
      JObject(JField(left._1, l) :: JField(right._1, r) :: Nil)
    }

    def ~(right: JObject) = {
      val l: JValue = left._2
      JObject(JField(left._1, l) :: right.obj)
    }
  }

  class JsonListAssoc(left: List[JField]) {
    def ~(right: (String, JValue)) = JObject(left ::: List(JField(right._1, right._2)))
    def ~(right: JObject) = JObject(left ::: right.obj)
  }
}