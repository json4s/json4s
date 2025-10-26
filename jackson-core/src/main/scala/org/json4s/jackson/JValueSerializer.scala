package org.json4s
package jackson

import org.json4s.JsonAST.*
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class JValueSerializer extends ValueSerializer[JValue] {
  def serialize(value: JValue, json: JsonGenerator, provider: SerializationContext): Unit = {
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
          elements foreach (x => serialize(x, json, provider))
          json.writeEndArray()

        case JSet(elements) =>
          json.writeStartArray()
          elements foreach (x => serialize(x, json, provider))
          json.writeEndArray()

        case JObject(fields) =>
          json.writeStartObject()
          fields foreach {
            case (_, JNothing) => ()
            case (n, v) =>
              json.writeName(n)
              serialize(v, json, provider)
          }
          json.writeEndObject()

        case JNull => json.writeNull()
        case JNothing => ()
      }
    }
  }

}
