package org.json4s

class ExtractableJsonAstNode(jv: JValue) {

  /**
   * Extract a value from a JSON.
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
    Extraction.extract(jv)(formats, mf)

  /**
   * Extract a value from a JSON.
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
    Extraction.extractOpt(jv)(formats, mf)

  /**
   * Extract a value from a JSON using a default value.
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
    Extraction.extractOpt(jv)(formats, mf).getOrElse(default)

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the org.json4s.JsonAST.JValue to an object of type `A`
   *
   * Example:
   * {{{
   *   case class Person(name: String)
   *   implicit object PersonReader extends Reader[Person] {
   *     def read(json: JValue): Person = Person((json \ "name").extract[String])
   *   }
   *   JObject(JField("name", JString("Joe")) :: Nil).as[Person]
   * }}}
   */
  def as[A](implicit reader: Reader[A]): A = reader.read(jv)

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the org.json4s.JsonAST.JValue to an object of type Option[`A`]
   *
   * Example:
   * {{{
   *   case class Person(name: String)
   *   implicit object PersonReader extends Reader[Person] {
   *     def read(json: JValue): Person = Person((json \ "name").extract[String])
   *   }
   *   JObject(JField("name", JString("Joe")) :: Nil).getAs[Person]
   * }}}
   */
  def getAs[A](implicit reader: Reader[A]): Option[A] = try {
    Option(reader.read(jv))
  } catch { case _: Throwable => None }

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the org.json4s.JsonAST.JValue to an object of type `A`
   * if an error occurs it will return the default value.
   *
   * Example:
   * {{{
   *   case class Person(name: String)
   *   implicit object PersonReader extends Reader[Person] {
   *     def read(json: JValue): Person = Person((json \ "name").extract[String])
   *   }
   *   JObject(JField("name", JString("Joe")) :: Nil).getAsOrElse(Person("Tom"))
   * }}}
   */
  def getAsOrElse[A](default: => A)(implicit reader: Reader[A]): A =
    getAs(reader) getOrElse default
}
