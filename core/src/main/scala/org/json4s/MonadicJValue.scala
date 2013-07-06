package org.json4s

import java.util.Locale.ENGLISH

class MonadicJValue(jv: JValue) {

  /**
   * XPath-like expression to query JSON fields by name. Matches only fields on
   * next level.
   * <p>
   * Example:<pre>
   * json \ "name"
   * </pre>
   */
  def \(nameToFind: String): JValue =
    findDirectByName(List(jv), nameToFind) match {
      case Nil ⇒ JNothing
      case x :: Nil ⇒ x
      case x ⇒ JArray(x)
    }

  private[this] def findDirectByName(xs: List[JValue], name: String): List[JValue] = xs.flatMap {
    case JObject(l) ⇒ l.filter {
      case (n, _) if n == name ⇒ true
      case _ ⇒ false
    } map (_._2)
    case JArray(l) ⇒ findDirectByName(l, name)
    case _ ⇒ Nil
  }

  private[this] def findDirect(xs: List[JValue], p: JValue ⇒ Boolean): List[JValue] = xs.flatMap {
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
    find(jv) match {
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
    findDirect(jv.children, typePredicate(clazz) _).asInstanceOf[List[A]] map { _.values }

  /**
   * XPath-like expression to query JSON fields by type. Returns all matching fields.
   * <p>
   * Example:<pre>
   * json \\ classOf[JInt]
   * </pre>
   */
  def \\[A <: JValue](clazz: Class[A]): List[A#Values] =
    (jv filter typePredicate(clazz) _).asInstanceOf[List[A]] map { _.values }

  private def typePredicate[A <: JValue](clazz: Class[A])(json: JValue) = json match {
    case x if x.getClass == clazz ⇒ true
    case _ ⇒ false
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
    rec(z, jv)
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
    rec(z, jv)
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
    rec(jv)
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
      case JObject(l) ⇒ JObject(l.map { case (n, va) ⇒ f(n -> rec(va)) })
      case JArray(l) ⇒ JArray(l.map(rec))
      case x ⇒ x
    }
    rec(jv)
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

    rep(l, jv)
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
      case JObject(fs) if (fs find p).isDefined ⇒ fs find p
      case JObject(fs) ⇒ fs.flatMap { case (n, v) ⇒ find(v) }.headOption
      case JArray(l) ⇒ l.flatMap(find _).headOption
      case _ ⇒ None
    }
    find(jv)
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
    find(jv)
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

  def withFilter(p: JValue => Boolean) = new JValueWithFilter(jv, p)
  class JValueWithFilter(self: JValue, p: JValue => Boolean) {
    def map[T](f: JValue => T): List[T] =
      self.filter(p).map(f)
    def flatMap[T](f: JValue => List[T]): List[T] =
      self.filter(p).flatMap(f)
    def foreach(f: JValue => Unit): Unit =
      self.filter(p).foreach(f)
    def withFilter(q: JValue => Boolean): JValueWithFilter =
      new JValueWithFilter(self, x => p(x) && q(x))
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
  def removeField(p: JField ⇒ Boolean): JValue = jv transform {
    case JObject(l) => JObject(l.filterNot(p))
  }

  /**
   * Return a JSON where all values matching the given predicate are removed.
   * <p>
   * Example:<pre>
   * JArray(JInt(1) :: JInt(2) :: JNull :: Nil) remove { _ == JNull }
   * </pre>
   */
  def remove(p: JValue ⇒ Boolean): JValue = {
    if(p(jv)) JNothing
    else jv transform {
      case JObject(l) => JObject(l.filterNot(f ⇒ p(f._2)))
      case JArray(l) => JArray(l.filterNot(p))
    }
  }

  private[this] def camelize(word: String): String = {
    val w = pascalize(word)
    w.substring(0, 1).toLowerCase(ENGLISH) + w.substring(1)
  }
  private[this] def pascalize(word: String): String = {
    val lst = word.split("_").toList
    (lst.headOption.map(s ⇒ s.substring(0, 1).toUpperCase(ENGLISH) + s.substring(1)).get ::
      lst.tail.map(s ⇒ s.substring(0, 1).toUpperCase + s.substring(1))).mkString("")
  }
  private[this] def underscore(word: String): String = {
    val spacesPattern = "[-\\s]".r
    val firstPattern = "([A-Z]+)([A-Z][a-z])".r
    val secondPattern = "([a-z\\d])([A-Z])".r
    val replacementPattern = "$1_$2"
    spacesPattern.replaceAllIn(
      secondPattern.replaceAllIn(
        firstPattern.replaceAllIn(
          word, replacementPattern), replacementPattern), "_").toLowerCase
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

  private[this] def rewriteJsonAST(camelize: Boolean): JValue =
    transformField {
      case JField(nm, x) if !nm.startsWith("_") ⇒ JField(if (camelize) this.camelize(nm) else underscore(nm), x)
    }

  /**
   * Remove the [[org.json4s.JsonAST.JNothing]] and [[org.json4s.JsonAST.JNull]] from
   * a [[org.json4s.JsonAST.JArray]] or [[org.json4s.JsonAST.JObject]]
   */
  def noNulls = remove {
    case JNull | JNothing => true
    case _ => false
  }

}
