package org.json4s
package jackson

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.DeserializationFeature.{USE_BIG_DECIMAL_FOR_FLOATS, USE_BIG_INTEGER_FOR_INTS}
import com.fasterxml.jackson.core.json._
import scala.util.control.Exception.allCatch
import org.json4s.prefs.EmptyValueStrategy

trait JsonMethods extends org.json4s.JsonMethods[JValue] {

  private[this] lazy val _defaultMapper = {
    val m = new ObjectMapper()
    m.registerModule(new Json4sScalaModule)
    // for backwards compatibility
    m.configure(USE_BIG_INTEGER_FOR_INTS, true)
    m
  }
  def mapper = _defaultMapper

  def parse[A: AsJsonInput](
    in: A,
    useBigDecimalForDouble: Boolean = false,
    useBigIntForLong: Boolean = true
  ): JValue = {
    var reader = mapper.readerFor(classOf[JValue])
    if (useBigDecimalForDouble)
      reader = reader `with` USE_BIG_DECIMAL_FOR_FLOATS
    else
      reader = reader `without` USE_BIG_DECIMAL_FOR_FLOATS

    if (useBigIntForLong)
      reader = reader `with` USE_BIG_INTEGER_FOR_INTS
    else
      reader = reader `without` USE_BIG_INTEGER_FOR_INTS

    AsJsonInput.asJsonInput(in) match {
      case StringInput(s) => reader.readValue(s)
      case ReaderInput(rdr) => reader.readValue(rdr)
      case StreamInput(stream) => reader.readValue(stream)
      case FileInput(file) => reader.readValue(file)
    }
  }

  def parseOpt[A: AsJsonInput](
    in: A,
    useBigDecimalForDouble: Boolean = false,
    useBigIntForLong: Boolean = true
  ): Option[JValue] = allCatch opt {
    parse(in, useBigDecimalForDouble, useBigIntForLong)
  }

  def render(
    value: JValue,
    alwaysEscapeUnicode: Boolean = false,
    emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default
  ): JValue = {
    if (mapper.isEnabled(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature()) != alwaysEscapeUnicode) {
      mapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), alwaysEscapeUnicode)
    }

    emptyValueStrategy.replaceEmpty(value)
  }

  def compact(d: JValue): String = mapper.writeValueAsString(d)

  def pretty(d: JValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }

  def asJValue[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)
  def fromJValue[T](json: JValue)(implicit reader: Reader[T]): T = reader.read(json)

  def asJsonNode(jv: JValue): JsonNode = mapper.valueToTree[JsonNode](jv)
  def fromJsonNode(jn: JsonNode): JValue = mapper.treeToValue[JValue](jn, classOf[JValue])

}

object JsonMethods extends JsonMethods
