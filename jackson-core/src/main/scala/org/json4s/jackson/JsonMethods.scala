package org.json4s
package jackson

import org.json4s.prefs.EmptyValueStrategy
import scala.util.control.Exception.allCatch
import tools.jackson.core.json.JsonWriteFeature
import tools.jackson.databind.*
import tools.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
import tools.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS
import tools.jackson.databind.json.JsonMapper

trait JsonMethods extends org.json4s.JsonMethods[JValue] {

  private def defaultMapperBuilder() = JsonMapper
    .builder()
    .addModule(
      new Json4sScalaModule
    )
    .configure(
      USE_BIG_INTEGER_FOR_INTS,
      true
    )

  private[this] lazy val _defaultMapper: JsonMapper =
    defaultMapperBuilder().build()

  private[this] lazy val mapperWithEscape: JsonMapper =
    defaultMapperBuilder().configure(JsonWriteFeature.ESCAPE_NON_ASCII, true).build()

  def mapper: ObjectMapper = _defaultMapper

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
      case StringInput(s) => reader.readValue[JValue](s)
      case ReaderInput(rdr) => reader.readValue[JValue](rdr)
      case StreamInput(stream) => reader.readValue[JValue](stream)
      case FileInput(file) => reader.readValue[JValue](file)
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
    emptyValueStrategy.replaceEmpty(value)
  }

  def compact(d: JValue): String = compact(d, false)

  def compact(d: JValue, alwaysEscapeUnicode: Boolean): String = {
    val m = if (alwaysEscapeUnicode) {
      mapperWithEscape
    } else {
      mapper
    }
    m.writeValueAsString(d)
  }

  def pretty(d: JValue): String =
    pretty(d, false)

  def pretty(d: JValue, alwaysEscapeUnicode: Boolean): String = {
    val m = if (alwaysEscapeUnicode) {
      mapperWithEscape
    } else {
      mapper
    }
    val writer = m.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }

  def asJValue[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)
  def fromJValue[T](json: JValue)(implicit reader: Reader[T]): T = reader.readEither(json) match {
    case Right(x) => x
    case Left(x) => throw x
  }

  def asJsonNode(jv: JValue): JsonNode = mapper.valueToTree[JsonNode](jv)
  def fromJsonNode(jn: JsonNode): JValue = mapper.treeToValue[JValue](jn, classOf[JValue])

}

object JsonMethods extends JsonMethods
