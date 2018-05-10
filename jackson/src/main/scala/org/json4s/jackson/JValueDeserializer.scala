package org.json4s.jackson

import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.{DeserializationFeature, DeserializationContext, JsonDeserializer}
import com.fasterxml.jackson.core.{JsonTokenId, JsonToken, JsonParser}
import collection.mutable
import org.json4s._

import scala.annotation.switch

class JValueDeserializer(factory: TypeFactory, klass: Class[_]) extends JsonDeserializer[Object] {
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): Object = {

    if (jp.getCurrentToken == null) jp.nextToken()

    val value = (jp.getCurrentToken.id(): @switch) match {
      case JsonTokenId.ID_NULL => JNull
      case JsonTokenId.ID_NUMBER_INT =>
        if (ctxt.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)) JInt(BigInt(jp.getText))
        else JLong(jp.getLongValue)
      case JsonTokenId.ID_NUMBER_FLOAT =>
        if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) JDecimal(BigDecimal(jp.getDecimalValue))
        else JDouble(jp.getDoubleValue)
      case JsonTokenId.ID_STRING => JString(jp.getText)
      case JsonTokenId.ID_TRUE => JBool.True
      case JsonTokenId.ID_FALSE => JBool.False

      case JsonTokenId.ID_START_ARRAY =>
        val values = new mutable.ListBuffer[JValue]()
        jp.nextToken()
        while(jp.getCurrentToken != JsonToken.END_ARRAY) {
          values += deserialize(jp, ctxt).asInstanceOf[JValue]
          jp.nextToken()
        }
        JArray(values.toList)

      case JsonTokenId.ID_START_OBJECT =>
        jp.nextToken()
        deserialize(jp, ctxt)

      case JsonTokenId.ID_FIELD_NAME | JsonTokenId.ID_END_OBJECT =>
        val fields = new mutable.ListBuffer[JField]
        while (jp.getCurrentToken.id() != JsonTokenId.ID_END_OBJECT) {
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
