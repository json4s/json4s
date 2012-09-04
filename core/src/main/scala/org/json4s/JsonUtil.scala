package org.json4s


trait JsonUtil[T] { self: JsonMethods[T] =>

  implicit protected def formats: Formats

  protected def serializer: Serialization

  import java.io.{Reader, StringWriter, Writer}
  /** Serialize to String.
   */
  def write[A <: AnyRef](a: A): String = serializer.write(a)

  /** Serialize to Writer.
   */
  def write[A <: AnyRef, W <: Writer](a: A, out: W): W = serializer.write(a, out)

  /** Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A): String = serializer.writePretty(a)

  /** Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: Writer](a: A, out: W): W = serializer.writePretty(a, out)

  /** Deserialize from a String.
   */
  def read[A](json: String)(implicit mf: Manifest[A]): A = serializer.read(json)

  /** Deserialize from a Reader.
   */
  def read[A](in: Reader)(implicit mf: Manifest[A]): A = serializer.read(in)


}
