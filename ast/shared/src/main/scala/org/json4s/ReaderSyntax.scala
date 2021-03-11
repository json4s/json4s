package org.json4s

class ReaderSyntax(private val jv: JValue) extends AnyVal {

  /**
   * Given that an implicit reader of type `A` is in scope
   * It will deserialize the org.json4s.JValue to an object of type `A`
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
   * It will deserialize the org.json4s.JValue to an object of type Option[`A`]
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
   * It will deserialize the org.json4s.JValue to an object of type `A`
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
