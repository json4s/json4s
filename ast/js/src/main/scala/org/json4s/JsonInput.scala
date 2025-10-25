package org.json4s

import java.io.InputStream

sealed abstract class JsonInput extends Product with Serializable {
  private[json4s] def toReader(): java.io.Reader = this match {
    case StringInput(x) =>
      new java.io.StringReader(x)
    case ReaderInput(x) =>
      x
    case StreamInput(x) =>
      new java.io.InputStreamReader(x, "UTF-8")
  }
}

case class StringInput(string: String) extends JsonInput
case class ReaderInput(reader: java.io.Reader) extends JsonInput
case class StreamInput(stream: InputStream) extends JsonInput
