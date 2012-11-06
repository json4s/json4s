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
  import mojolly.inflector.InflectorImports._

  /**
   * Concatenates a sequence of <code>JValue</code>s.
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

    /**
     * XPath-like expression to query JSON fields by name. Matches only fields on
     * next level.
     * <p>
     * Example:<pre>
     * json \ "name"
     * </pre>
     */
    def \(nameToFind: String): JValue =
      findDirectByName(List(this), nameToFind) match {
        case Nil ⇒ JNothing
        case x :: Nil ⇒ x
        case x ⇒ JArray(x)
      }

    private def findDirectByName(xs: List[JValue], name: String): List[JValue] = xs.flatMap {
      case JObject(l) ⇒ l.filter {
        case (n, _) if n == name ⇒ true
        case _ ⇒ false
      } map (_._2)
      case JArray(l) ⇒ findDirectByName(l, name)
      case _ ⇒ Nil
    }

    private def findDirect(xs: List[JValue], p: JValue ⇒ Boolean): List[JValue] = xs.flatMap {
      case JObject(l) ⇒ l.filter {
        case (n, x) if p(x) ⇒ true
        case _ ⇒ false
      } map (_._2)
      case JArray(l) ⇒ findDirect(l, p)
      case x if p(x) ⇒ x :: Nil
      case _ ⇒ Nil
    }

    /**
     * XPath-like expression to query JSON fields by name. Returns all matching fields.
     * <p>
     * Example:<pre>
     * json \\ "name"
     * </pre>
     */
    def \\(nameToFind: String): JValue = {
      def find(json: JValue): List[JField] = json match {
        case JObject(l) ⇒ l.foldLeft(List[JField]()) {
          case (a, (name, value)) ⇒
            if (name == nameToFind) a ::: List((name, value)) ::: find(value) else a ::: find(value)
        }
        case JArray(l) ⇒ l.foldLeft(List[JField]())((a, json) ⇒ a ::: find(json))
        case _ ⇒ Nil
      }
      find(this) match {
        case (_, x) :: Nil ⇒ x
        case xs ⇒ JObject(xs)
      }
    }

    /**
     * XPath-like expression to query JSON fields by type. Matches only fields on
     * next level.
     * <p>
     * Example:<pre>
     * json \ classOf[JInt]
     * </pre>
     */
    def \[A <: JValue](clazz: Class[A]): List[A#Values] =
      findDirect(children, typePredicate(clazz) _).asInstanceOf[List[A]] map { _.values }

    /**
     * XPath-like expression to query JSON fields by type. Returns all matching fields.
     * <p>
     * Example:<pre>
     * json \\ classOf[JInt]
     * </pre>
     */
    def \\[A <: JValue](clazz: Class[A]): List[A#Values] =
      (this filter typePredicate(clazz) _).asInstanceOf[List[A]] map { _.values }

    private def typePredicate[A <: JValue](clazz: Class[A])(json: JValue) = json match {
      case x if x.getClass == clazz ⇒ true
      case _ ⇒ false
    }

    /**
     * Return nth element from JSON.
     * Meaningful only to JArray, JObject and JField. Returns JNothing for other types.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil)(1) == JInt(2)
     * </pre>
     */
    def apply(i: Int): JValue = JNothing

    /**
     * Return unboxed values from JSON
     * <p>
     * Example:<pre>
     * JObject(JField("name", JString("joe")) :: Nil).values == Map("name" -> "joe")
     * </pre>
     */
    def values: Values

    /**
     * Return direct child elements.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil).children == List(JInt(1), JInt(2))
     * </pre>
     */
    def children: List[JValue] = this match {
      case JObject(l) ⇒ l map (_._2)
      case JArray(l) ⇒ l
      case _ ⇒ Nil
    }

    /**
     * Return a combined value by folding over JSON by applying a function <code>f</code>
     * for each element. The initial value is <code>z</code>.
     */
    def fold[A](z: A)(f: (A, JValue) ⇒ A): A = {
      def rec(acc: A, v: JValue) = {
        val newAcc = f(acc, v)
        v match {
          case JObject(l) ⇒ l.foldLeft(newAcc) { case (a, (name, value)) ⇒ value.fold(a)(f) }
          case JArray(l) ⇒ l.foldLeft(newAcc)((a, e) ⇒ e.fold(a)(f))
          case _ ⇒ newAcc
        }
      }
      rec(z, this)
    }

    /**
     * Return a combined value by folding over JSON by applying a function <code>f</code>
     * for each field. The initial value is <code>z</code>.
     */
    def foldField[A](z: A)(f: (A, JField) ⇒ A): A = {
      def rec(acc: A, v: JValue) = {
        v match {
          case JObject(l) ⇒ l.foldLeft(acc) {
            case (a, field @ (name, value)) ⇒ value.foldField(f(a, field))(f)
          }
          case JArray(l) ⇒ l.foldLeft(acc)((a, e) ⇒ e.foldField(a)(f))
          case _ ⇒ acc
        }
      }
      rec(z, this)
    }

    /**
     * Return a new JValue resulting from applying the given function <code>f</code>
     * to each value in JSON.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) map {
     *   case JInt(x) => JInt(x+1)
     *   case x => x
     * }
     * </pre>
     */
    def map(f: JValue ⇒ JValue): JValue = {
      def rec(v: JValue): JValue = v match {
        case JObject(l) ⇒ f(JObject(l.map { case (n, va) ⇒ (n, rec(va)) }))
        case JArray(l) ⇒ f(JArray(l.map(rec)))
        case x ⇒ f(x)
      }
      rec(this)
    }

    /**
     * Return a new JValue resulting from applying the given function <code>f</code>
     * to each field in JSON.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) map {
     *   case ("age", JInt(x)) => ("age", JInt(x+1))
     *   case x => x
     * }
     * </pre>
     */
    def mapField(f: JField ⇒ JField): JValue = {
      def rec(v: JValue): JValue = v match {
        case JObject(l) ⇒ JObject(l.map { case (n, va) ⇒ f(n, rec(va)) })
        case JArray(l) ⇒ JArray(l.map(rec))
        case x ⇒ x
      }
      rec(this)
    }

    /**
     * Return a new JValue resulting from applying the given partial function <code>f</code>
     * to each field in JSON.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) transformField {
     *   case ("age", JInt(x)) => ("age", JInt(x+1))
     * }
     * </pre>
     */
    def transformField(f: PartialFunction[JField, JField]): JValue = mapField { x ⇒
      if (f.isDefinedAt(x)) f(x) else x
    }

    /**
     * Return a new JValue resulting from applying the given partial function <code>f</code>
     * to each value in JSON.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) transform { case JInt(x) => JInt(x+1) }
     * </pre>
     */
    def transform(f: PartialFunction[JValue, JValue]): JValue = map { x ⇒
      if (f.isDefinedAt(x)) f(x) else x
    }

    /**
     * Return a new JValue resulting from replacing the value at the specified field
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
          case x :: xs ⇒ in match {
            case JObject(fields) ⇒ JObject(
              fields.map {
                case JField(`x`, value) ⇒ JField(x, if (xs == Nil) replacement else rep(xs, value))
                case field ⇒ field
              })
            case other ⇒ other
          }

          case Nil ⇒ in
        }
      }

      rep(l, this)
    }

    /**
     * Return the first field from JSON which matches the given predicate.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(2))) findField { case (n, v) => n == "age" }
     * </pre>
     */
    def findField(p: JField ⇒ Boolean): Option[JField] = {
      def find(json: JValue): Option[JField] = json match {
        case JObject(fs) if (fs find p).isDefined ⇒ return fs find p
        case JObject(fs) ⇒ fs.flatMap { case (n, v) ⇒ find(v) }.headOption
        case JArray(l) ⇒ l.flatMap(find _).headOption
        case _ ⇒ None
      }
      find(this)
    }

    /**
     * Return the first element from JSON which matches the given predicate.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) find { _ == JInt(2) } == Some(JInt(2))
     * </pre>
     */
    def find(p: JValue ⇒ Boolean): Option[JValue] = {
      def find(json: JValue): Option[JValue] = {
        if (p(json)) return Some(json)
        json match {
          case JObject(fs) ⇒ fs.flatMap { case (n, v) ⇒ find(v) }.headOption
          case JArray(l) ⇒ l.flatMap(find _).headOption
          case _ ⇒ None
        }
      }
      find(this)
    }

    /**
     * Return a List of all fields which matches the given predicate.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) filterField {
     *   case ("age", JInt(x)) if x > 18 => true
     *   case _          => false
     * }
     * </pre>
     */
    def filterField(p: JField ⇒ Boolean): List[JField] =
      foldField(List[JField]())((acc, e) ⇒ if (p(e)) e :: acc else acc).reverse

    /**
     * Return a List of all values which matches the given predicate.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) filter { case JInt(x) => x > 1; case _ => false }
     * </pre>
     */
    def filter(p: JValue ⇒ Boolean): List[JValue] =
      fold(List[JValue]())((acc, e) ⇒ if (p(e)) e :: acc else acc).reverse

    /**
     * Concatenate with another JSON.
     * This is a concatenation monoid: (JValue, ++, JNothing)
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) ++ JArray(JInt(3) :: Nil) ==
     * JArray(List(JInt(1), JInt(2), JInt(3)))
     * </pre>
     */
    def ++(other: JValue) = {
      def append(value1: JValue, value2: JValue): JValue = (value1, value2) match {
        case (JNothing, x) ⇒ x
        case (x, JNothing) ⇒ x
        case (JArray(xs), JArray(ys)) ⇒ JArray(xs ::: ys)
        case (JArray(xs), v: JValue) ⇒ JArray(xs ::: List(v))
        case (v: JValue, JArray(xs)) ⇒ JArray(v :: xs)
        case (x, y) ⇒ JArray(x :: y :: Nil)
      }
      append(this, other)
    }

    /**
     * Return a JSON where all fields matching the given predicate are removed.
     * <p>
     * Example:<pre>
     * JObject(("age", JInt(10)) :: Nil) removeField {
     *   case ("age", _) => true
     *   case _          => false
     * }
     * </pre>
     */
    def removeField(p: JField ⇒ Boolean): JValue = this mapField {
      case x if p(x) ⇒ (x._1, JNothing)
      case x ⇒ x
    }

    /**
     * Return a JSON where all values matching the given predicate are removed.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: JNull :: Nil) remove { _ == JNull }
     * </pre>
     */
    def remove(p: JValue ⇒ Boolean): JValue = this map {
      case x if p(x) ⇒ JNothing
      case x ⇒ x
    }

    /**
     * Camelize all the keys in this [[org.json4s.JsonAST.JValue]]
     */
    def camelizeKeys = rewriteJsonAST(camelize = true)
    
    /**
     * Underscore all the keys in this [[org.json4s.JsonAST.JValue]]
     */
    def snakizeKeys = rewriteJsonAST(camelize = false)

        
    /**
     * Underscore all the keys in this [[org.json4s.JsonAST.JValue]]
     */
    def underscoreKeys = snakizeKeys

    private[this] def rewriteJsonAST(camelize: Boolean): JValue = {
      transformField {
        case JField(nm, x) if !nm.startsWith("_") ⇒ JField(if (camelize) nm.camelize else nm.underscore, x)
        case x ⇒ x
      }
    }

    /**
     * Remove the [[org.json4s.JsonAST.JNothing]] and [[org.json4s.JsonAST.JNull]] from
     * a [[org.json4s.JsonAST.JArray]] or [[org.json4s.JsonAST.JObject]]
     */
    def noNulls = removeNulls(this)

    private[this] def removeNulls(initial: JValue): JValue = {
      initial match {
        case JArray(values) ⇒ JArray(values map removeNulls)
        case j: JObject ⇒ removeNullsFromJObject(j)
        case _ ⇒ initial
      }
    }

    private[this] def removeNullsFromJObject(initial: JObject): JValue = JObject(initial filterField valueIsNotNull)
    private[this] def valueIsNotNull(field: JField): Boolean = field._2 != JNothing && field._2 != JNull

    def toOpt: Option[JValue] = this match {
      case JNothing ⇒ None
      case json ⇒ Some(json)
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
  trait JNumber extends JValue 
  case class JDouble(num: Double) extends JNumber {
    type Values = Double
    def values = num
  }
  case class JDecimal(num: BigDecimal) extends JNumber {
    type Values = BigDecimal
    def values = num
  }
  case class JInt(num: BigInt) extends JNumber {
    type Values = BigInt
    def values = num
  }
  case class JBool(value: Boolean) extends JValue {
    type Values = Boolean
    def values = value
  }

  case class JObject(obj: List[JField]) extends JValue {
    type Values = Map[String, Any]
    def values = obj.map { case (n, v) ⇒ (n, v.values) } toMap

    override def equals(that: Any): Boolean = that match {
      case o: JObject ⇒ Set(obj.toArray: _*) == Set(o.obj.toArray: _*)
      case _ ⇒ false
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
//
//  case class LazyJArray(arr: Stream[JValue]) extends JValue {
//    type Values = Stream[Any]
//    def values = arr.map(_.values)
//    override def apply(i: Int): JValue = arr(i)
//  }

  type JField = (String, JValue)
  object JField {
    def apply(name: String, value: JValue) = (name, value)
    def unapply(f: JField): Option[(String, JValue)] = Some(f)
  }

  private[json4s] def quote(s: String): String = {
    val buf = new StringBuilder
    for (i ← 0 until s.length) {
      val c = s.charAt(i)
      buf.append(c match {
        case '"' ⇒ "\\\""
        case '\\' ⇒ "\\\\"
        case '\b' ⇒ "\\b"
        case '\f' ⇒ "\\f"
        case '\n' ⇒ "\\n"
        case '\r' ⇒ "\\r"
        case '\t' ⇒ "\\t"
        case c if ((c >= '\u0000' && c < '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) ⇒ "\\u%04x".format(c: Int)
        case c ⇒ c
      })
    }
    buf.toString
  }
}

