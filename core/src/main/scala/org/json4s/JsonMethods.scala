package org.json4s

import java.io.{Reader => JReader, File, InputStream}

sealed abstract class JsonInput extends Product with Serializable
case class StringInput(string: String) extends JsonInput
case class ReaderInput(reader: JReader) extends JsonInput
case class StreamInput(stream: InputStream) extends JsonInput
case class FileInput(file: File) extends JsonInput

trait JsonMethods[T] {

  def parse[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false, useBigIntForLong: Boolean = true): JValue
  def parseOpt[A: AsJsonInput](
    in: A,
    useBigDecimalForDouble: Boolean = false,
    useBigIntForLong: Boolean = true
  ): Option[JValue]

  def render(value: JValue)(implicit formats: Formats = DefaultFormats): T
  def compact(d: T): String
  def pretty(d: T): String
}
