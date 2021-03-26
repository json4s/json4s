package org.json4s

import java.io.{Reader => JReader, File, InputStream}

sealed abstract class JsonInput extends Product with Serializable
case class StringInput(string: String) extends JsonInput
case class ReaderInput(reader: JReader) extends JsonInput
case class StreamInput(stream: InputStream) extends JsonInput
case class FileInput(file: File) extends JsonInput
