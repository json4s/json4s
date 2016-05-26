package org.json4s
package jackson

import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}
import com.fasterxml.jackson.core.JsonGenerator

class JValueSerializer extends JsonSerializer[JValue]{
  def serialize(value: JValue, json: JsonGenerator, provider: SerializerProvider): Unit = {
    if (value == null) {
      json.writeNull()
    } else {
      value match {
        case JInt(v) => json.writeNumber(v.bigInteger)
        case JLong(v) => json.writeNumber(v)
        case JDouble(v) => json.writeNumber(v)
        case JDecimal(v) => json.writeNumber(v.bigDecimal)
        case JString(v) => json.writeString(v)
        case JBool(v) => json.writeBoolean(v)
        case JArray(elements) =>
          json.writeStartArray()
          elements filterNot (_ == JNothing) foreach (x => serialize(x, json, provider))
          json.writeEndArray()

        case JObject(fields) => {
          json.writeStartObject()
          fields filterNot (_._2 == JNothing) foreach {
            case (n, v) =>
              json.writeFieldName(n)
              serialize(v, json, provider)
          }
          json.writeEndObject()
        }
        case JNull => json.writeNull()
        case JNothing => ()
      }
    }
  }

  override def isEmpty(value: JValue): Boolean = value == JNothing
}
