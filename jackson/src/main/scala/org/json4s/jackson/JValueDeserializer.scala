package org.json4s.jackson

import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.{DeserializationFeature, DeserializationContext, JsonDeserializer}
import com.fasterxml.jackson.core.{FormatSchema, JsonToken, JsonParser}
import collection.mutable
import org.json4s._

class JValueDeserializer(factory: TypeFactory, klass: Class[_]) extends JsonDeserializer[Object] {
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): Object = {

    if (jp.getCurrentToken == null) jp.nextToken()

    val value = jp.getCurrentToken match {
      case JsonToken.VALUE_NULL => JNull
      case JsonToken.VALUE_NUMBER_INT => JInt(BigInt(jp.getText))
      case JsonToken.VALUE_NUMBER_FLOAT =>
        if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) JDecimal(BigDecimal(jp.getDecimalValue))
        else JDouble(jp.getDoubleValue)
      case JsonToken.VALUE_STRING => JString(jp.getText)
      case JsonToken.VALUE_TRUE => JBool(true)
      case JsonToken.VALUE_FALSE => JBool(false)

      case JsonToken.START_ARRAY =>
        val values = new mutable.MutableList[JValue]()
        jp.nextToken()
        while(jp.getCurrentToken != JsonToken.END_ARRAY) {
          values += deserialize(jp, ctxt).asInstanceOf[JValue]
          jp.nextToken()
        }
        JArray(values.toList)

      case JsonToken.START_OBJECT =>
        jp.nextToken()
        deserialize(jp, ctxt)

      case JsonToken.FIELD_NAME | JsonToken.END_OBJECT =>
        val fields = new mutable.MutableList[JField]
        while (jp.getCurrentToken != JsonToken.END_OBJECT) {
          val name = jp.getCurrentName
          jp.nextToken()
          fields += JField(name, deserialize(jp, ctxt).asInstanceOf[JValue])
          jp.nextToken()
        }
        JObject(fields.toList)

      case _ => throw ctxt.mappingException(classOf[JValue])
    }

    if (!klass.isAssignableFrom(value.getClass)) throw ctxt.mappingException(klass)

    value
  }

  override def isCachable = true

  override def getNullValue: Object = JNull

  override def getEmptyValue: Object = JNothing
}
