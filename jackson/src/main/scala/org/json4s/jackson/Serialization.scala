package org.json4s
package jackson

import scala.reflect.Manifest
import java.io.OutputStream

/**
 * Functions to serialize and deserialize a case class.
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
  import java.io.{ Reader, Writer }
  /**
   * Serialize to String.
   */
  def write[A <: AnyRef](a: A)(implicit formats: Formats): String =
    JsonMethods.mapper.writeValueAsString(Extraction.decompose(a))

  /**
   * Serialize to Writer.
   */
  def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    JsonMethods.mapper.writeValue(out, Extraction.decompose(a))
    out
  }

  def write[A <: AnyRef](a: A, out: OutputStream)(implicit formats: Formats) {
    JsonMethods.mapper.writeValue(out, Extraction.decompose(a))
  }

  /**
   * Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String =
    JsonMethods.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Extraction.decompose(a))

  /**
   * Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    JsonMethods.mapper.writerWithDefaultPrettyPrinter.writeValue(out, Extraction.decompose(a))
    out
  }

  /**
   * Deserialize from a String.
   */
  def read[A](json: String)(implicit formats: Formats, mf: Manifest[A]): A =
    JsonMethods.parse(json, formats.wantsBigDecimal).extract(formats, mf)

  @deprecated("You can use formats now to indicate you want to use decimals instead of doubles", "3.2.0")
  def read[A](json: String, useBigDecimalForDouble: Boolean)(implicit formats: Formats, mf: Manifest[A]): A =
    if (useBigDecimalForDouble) read(json)(formats.withBigDecimal, mf) else read(json)(formats.withDouble, mf)

  /**
   * Deserialize from a Reader.
   */
  @deprecated("You can use formats now to indicate you want to use decimals instead of doubles", "3.2.0")
  def read[A](in: Reader, useBigDecimalForDouble: Boolean)(implicit formats: Formats, mf: Manifest[A]): A = {
    if (useBigDecimalForDouble) read(in)(formats.withBigDecimal, mf) else read(in)(formats.withDouble, mf)
  }
  /**
   * Deserialize from a Reader.
   */
  def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A = {
    JsonMethods.parse(in, formats.wantsBigDecimal).extract(formats, mf)
  }
}
