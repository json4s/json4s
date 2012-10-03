package org.json4s

import java.io.{Reader => JReader, File, InputStream}

sealed trait JsonInput
case class StringInput(string: String) extends JsonInput
case class ReaderInput(reader: JReader) extends JsonInput
case class StreamInput(stream: InputStream) extends JsonInput
case class FileInput(file: File) extends JsonInput


trait JsonMethods[T] {

  def parseJson(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue
  def parseOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue]

  def render(value: JValue): T
  def compact(d: T): String
  def pretty(d: T): String
}
