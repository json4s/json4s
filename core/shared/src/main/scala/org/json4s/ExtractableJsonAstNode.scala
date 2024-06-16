package org.json4s

class ExtractableJsonAstNode(private val jv: JValue) extends AnyVal {

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

}
