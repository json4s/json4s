package org.json4s
package jackson

import com.fasterxml.jackson.databind.ObjectMapper
import util.control.Exception.allCatch
import org.json4s
import io.Source

trait JsonMethods extends json4s.JsonMethods[JValue] {

  private lazy val _defaultMapper = {
    val m = new ObjectMapper()
    m.registerModule(new Json4sScalaModule)
    m
  }
  def mapper = _defaultMapper

  def parse(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue = in match {
    case StringInput(s) => mapper.readValue(s, classOf[JValue])
    case ReaderInput(rdr) => mapper.readValue(rdr, classOf[JValue])
    case StreamInput(stream) => mapper.readValue(stream, classOf[JValue])
    case FileInput(file) => mapper.readValue(file, classOf[JValue])
  }

  def parseOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue] =  allCatch opt {
    parse(in, useBigDecimalForDouble)
  }

  def render(value: JValue): JValue = value

  def compact(d: JValue): String = mapper.writeValueAsString(d)

  def pretty(d: JValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }



}
object JsonMethods extends JsonMethods
