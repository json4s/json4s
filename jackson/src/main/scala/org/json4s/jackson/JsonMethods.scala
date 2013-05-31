package org.json4s
package jackson

import com.fasterxml.jackson.databind.{ ObjectMapper, DeserializationFeature }
import util.control.Exception.allCatch
import com.fasterxml.jackson.databind.SerializationFeature
import org.json4s.prefs.EmptyValueStrategy

trait JsonMethods extends org.json4s.JsonMethods[JValue] {

  private[this] lazy val _defaultMapper = {
    val m = new ObjectMapper()
    m.registerModule(new Json4sScalaModule)
    m
  }
  def mapper = _defaultMapper

  def parse(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue = {
    // what about side effects? (mkubala)
    mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, useBigDecimalForDouble)
    in match {
      case StringInput(s) => mapper.readValue(s, classOf[JValue])
      case ReaderInput(rdr) => mapper.readValue(rdr, classOf[JValue])
      case StreamInput(stream) => mapper.readValue(stream, classOf[JValue])
      case FileInput(file) => mapper.readValue(file, classOf[JValue])
    }
  }

  def parseOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue] = allCatch opt {
    parse(in, useBigDecimalForDouble)
  }

  def render(value: JValue)(implicit emptyStrategy: EmptyValueStrategy = EmptyValueStrategy.default): JValue =
    emptyStrategy(value)

  def compact(d: JValue): String = mapper.writeValueAsString(d)

  def pretty(d: JValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }

  def asJValue[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)
  def fromJValue[T](json: JValue)(implicit reader: Reader[T]): T = reader.read(json)
}

object JsonMethods extends JsonMethods
