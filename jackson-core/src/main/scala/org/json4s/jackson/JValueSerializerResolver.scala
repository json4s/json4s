package org.json4s
package jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.Serializers

private object JValueSerializerResolver extends Serializers.Base {
  private[this] val JVALUE = classOf[JValue]
  override def findSerializer(
    config: SerializationConfig,
    theType: JavaType,
    beanDesc: BeanDescription
  ): JsonSerializer[?] = {
    if (!JVALUE.isAssignableFrom(theType.getRawClass)) null
    else new JValueSerializer
  }

}
