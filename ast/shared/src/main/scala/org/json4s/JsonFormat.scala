package org.json4s

import annotation.implicitNotFound

object JsonFormat extends FormatFunctions {
  implicit def GenericFormat[T](implicit reader: Reader[T], writer: Writer[T]): JsonFormat[T] = new JsonFormat[T] {
    def write(obj: T): JValue = writer.write(obj)
    def read(value: JValue): T = reader.read(value)
    def readEither(value: JValue) = reader.readEither(value)
  }
}

@implicitNotFound(
  "No Json formatter found for type ${T}. Try to implement an implicit JsonFormat for this type."
)
trait JsonFormat[T] extends Writer[T] with Reader[T]
trait BigDecimalJsonFormats extends DefaultJsonFormats with DefaultReaders with BigDecimalWriters
trait DoubleJsonFormats extends DefaultJsonFormats with DefaultReaders with DoubleWriters
object BigDecimalJsonFormats extends BigDecimalJsonFormats
object DoubleJsonFormats extends DoubleJsonFormats
object DefaultJsonFormats extends DoubleJsonFormats
trait DefaultJsonFormats extends DefaultReaders
