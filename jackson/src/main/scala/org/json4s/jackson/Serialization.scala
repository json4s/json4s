package org.json4s
package jackson

import reflect.Manifest

/** Functions to serialize and deserialize a case class.
 * Custom serializer can be inserted if a class is not a case class.
 * <p>
 * Example:<pre>
 * val hints = new ShortTypeHints( ... )
 * implicit val formats = Serialization.formats(hints)
 * </pre>
 *
 * @see org.json4s.TypeHints
 */
object Serialization extends Serialization {
  import java.io.{Reader, StringWriter, Writer}
  /** Serialize to String.
   */
  def write[A <: AnyRef](a: A)(implicit formats: Formats): String =
    (write(a, new StringWriter)(formats)).toString

  /** Serialize to Writer.
   */
  def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    JsonMethods.mapper.writeValue(out, Extraction.decompose(a)(formats))
    out
  }

  /** Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String =
    (writePretty(a, new StringWriter)(formats)).toString

  /** Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    JsonMethods.mapper.writerWithDefaultPrettyPrinter.writeValue(out, Extraction.decompose(a)(formats))
    out
  }

  /** Deserialize from a String.
   */
  def read[A](json: String)(implicit formats: Formats, mf: Manifest[A]): A =
    JsonMethods.parseJson(json).extract(formats, mf)

  /** Deserialize from a Reader.
   */
  def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A =
    JsonMethods.mapper.readValue[JValue](in, classOf[JValue]).extract(formats, mf)
}