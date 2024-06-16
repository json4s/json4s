package org.json4s

import scala.reflect.Manifest

trait Serialization {
  import java.io.{Writer => JavaWriter}

  /**
   * Serialize to String.
   */
  def write[A <: AnyRef](a: A)(implicit formats: Formats): String

  /**
   * Serialize to Writer.
   */
  def write[A <: AnyRef, W <: JavaWriter](a: A, out: W)(implicit formats: Formats): W

  /**
   * Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String

  /**
   * Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: JavaWriter](a: A, out: W)(implicit formats: Formats): W

  /**
   * Deserialize from a String.
   */
  def read[A](json: String)(implicit formats: Formats, mf: Manifest[A]): A = read(StringInput(json))

  /**
   * Deserialize from an JsonInput
   */
  def read[A](json: JsonInput)(implicit formats: Formats, mf: Manifest[A]): A

  /**
   * Create Serialization formats with given type hints.
   * <p>
   * Example:<pre>
   * val hints = new ShortTypeHints( ... )
   * implicit val formats: Formats = Serialization.formats(hints)
   * </pre>
   */
  def formats(hints: TypeHints) = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = hints
  }
}
