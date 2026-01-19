package org.json4s.jackson

import collection.mutable
import org.json4s.*
import org.json4s.JsonAST.JField
import scala.annotation.switch
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.JsonTokenId
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ValueDeserializer

class JValueDeserializer(klass: Class[?]) extends ValueDeserializer[Object] {
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): Object = {

    def _deserialize(jp: JsonParser, ctxt: DeserializationContext): Object = {

      if (jp.currentToken() == null) jp.nextToken()

      (jp.currentToken().id(): @switch) match {
        case JsonTokenId.ID_NULL => JNull
        case JsonTokenId.ID_NUMBER_INT =>
          if (ctxt.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)) JInt(BigInt(jp.getString()))
          else JLong(jp.getLongValue)
        case JsonTokenId.ID_NUMBER_FLOAT =>
          if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS))
            JDecimal(BigDecimal(jp.getDecimalValue))
          else JDouble(jp.getDoubleValue)
        case JsonTokenId.ID_STRING => JString(jp.getString)
        case JsonTokenId.ID_TRUE => JBool.True
        case JsonTokenId.ID_FALSE => JBool.False

        case JsonTokenId.ID_START_ARRAY =>
          val values = new mutable.ListBuffer[JValue]()
          jp.nextToken()
          while (jp.currentToken() != JsonToken.END_ARRAY) {
            values += _deserialize(jp, ctxt).asInstanceOf[JValue]
            jp.nextToken()
          }
          JArray(values.toList)

        case JsonTokenId.ID_START_OBJECT =>
          jp.nextToken()
          _deserialize(jp, ctxt)

        case JsonTokenId.ID_PROPERTY_NAME | JsonTokenId.ID_END_OBJECT =>
          val fields = new mutable.ListBuffer[JField]
          while (jp.currentToken().id() != JsonTokenId.ID_END_OBJECT) {
            val name = jp.currentName()
            jp.nextToken()
            fields += JField(name, _deserialize(jp, ctxt).asInstanceOf[JValue])
            jp.nextToken()
          }
          JObject(fields.toList)

        case _ => ctxt.handleUnexpectedToken(classOf[JValue], jp)
      }
    }

    val value = _deserialize(jp, ctxt)

    if (!klass.isAssignableFrom(value.getClass)) ctxt.handleUnexpectedToken(klass, jp)

    value
  }

  override def isCachable = true

}
