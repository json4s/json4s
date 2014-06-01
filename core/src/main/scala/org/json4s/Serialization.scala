package org.json4s

import scala.reflect.Manifest

trait Serialization {
  import java.io.{Reader, Writer}
  /** Serialize to String.
   */
  def write[A <: AnyRef](a: A)(implicit formats: Formats): String

  /** Serialize to Writer.
   */
  def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W

  /** Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String

  /** Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W

  /** Deserialize from a String.
   */
  @deprecated("You can use formats now to indicate you want to use decimals instead of doubles", "3.2.0")
  def read[A](json: String, useBigDecimalForDouble: Boolean)(implicit formats: Formats, mf: Manifest[A]): A

  /** Deserialize from a String.
   */
  def read[A](json: String)(implicit formats: Formats, mf: Manifest[A]): A

  /** Deserialize from a Reader.
   */
  @deprecated("You can use formats now to indicate you want to use decimals instead of doubles", "3.2.0")
  def read[A](in: Reader, useBigDecimalForDouble: Boolean)(implicit formats: Formats, mf: Manifest[A]): A

  /** Create Serialization formats with given type hints.
   * <p>
   * Example:<pre>
   * val hints = new ShortTypeHints( ... )
   * implicit val formats = Serialization.formats(hints)
   * </pre>
   */
  def formats(hints: TypeHints) = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = hints
  }
}
