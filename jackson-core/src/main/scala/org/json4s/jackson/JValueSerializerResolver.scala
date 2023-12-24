package org.json4s
package jackson

import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.{BeanDescription, JavaType, JsonSerializer, SerializationConfig}

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
