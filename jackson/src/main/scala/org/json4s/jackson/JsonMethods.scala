package org.json4s
package jackson

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
import scala.util.control.Exception.allCatch

trait JsonMethods extends org.json4s.JsonMethods[JValue] {

  private[this] lazy val _defaultMapper = {
    val m = new ObjectMapper()
    m.registerModule(new Json4sScalaModule)
    m
  }
  def mapper = _defaultMapper

  def parse(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue = {
    val reader = if (useBigDecimalForDouble)
      mapper.reader[ObjectReader](classOf[JValue]) `with` USE_BIG_DECIMAL_FOR_FLOATS
    else mapper.reader[ObjectReader](classOf[JValue])

    in match {
	    case StringInput(s) => reader.readValue(s)
	    case ReaderInput(rdr) => reader.readValue(rdr)
	    case StreamInput(stream) => reader.readValue(stream)
	    case FileInput(file) => reader.readValue(file)
	  }
  }

  def parseOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue] =  allCatch opt {
    parse(in, useBigDecimalForDouble)
  }

  def render(value: JValue)(implicit formats: Formats = DefaultFormats): JValue =
    formats.emptyValueStrategy.replaceEmpty(value)

  def compact(d: JValue): String = mapper.writeValueAsString(d)

  def pretty(d: JValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter[ObjectWriter]()
    writer.writeValueAsString(d)
  }


  def asJValue[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)
  def fromJValue[T](json: JValue)(implicit reader: Reader[T]): T = reader.read(json)

  def asJsonNode(jv: JValue): JsonNode = mapper.valueToTree[JsonNode](jv)
  def fromJsonNode(jn: JsonNode): JValue = mapper.treeToValue[JValue](jn, classOf[JValue])

}

object JsonMethods extends JsonMethods