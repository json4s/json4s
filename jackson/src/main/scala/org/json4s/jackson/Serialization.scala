package org.json4s
package jackson

import scala.reflect.Manifest
import java.io.OutputStream

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
  import java.io.{Reader, Writer}
  /** Serialize to String.
   */
  def write[A <: AnyRef](a: A)(implicit formats: Formats): String =
    JsonMethods.mapper.writeValueAsString(Extraction.decompose(a)(formats))

  /** Serialize to Writer.
   */
  def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    JsonMethods.mapper.writeValue(out, Extraction.decompose(a)(formats))
    out
  }

  def write[A <: AnyRef](a: A, out: OutputStream)(implicit formats: Formats): Unit = {
    JsonMethods.mapper.writeValue(out, Extraction.decompose(a)(formats: Formats))
  }

  /** Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String =
    JsonMethods.mapper.writerWithDefaultPrettyPrinter.writeValueAsString(Extraction.decompose(a)(formats))

  /** Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    JsonMethods.mapper.writerWithDefaultPrettyPrinter.writeValue(out, Extraction.decompose(a)(formats))
    out
  }

  /** Deserialize from an JsonInput
    */
  def read[A](json: JsonInput)(implicit formats: Formats, mf: Manifest[A]): A =
    JsonMethods.parse(json, formats.wantsBigDecimal, formats.wantsBigInt).extract(formats, mf)

  /** Deserialize from a Reader.
   */
  def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A = {
    JsonMethods.parse(in, formats.wantsBigDecimal, formats.wantsBigInt).extract(formats, mf)
  }
}
