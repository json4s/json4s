package org.json4s
package jackson

import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.{BeanDescription, JavaType, SerializationConfig}

private object JValueSerializerResolver extends Serializers.Base {
  private val JVALUE = classOf[JValue]
  override def findSerializer(config: SerializationConfig, theType: JavaType, beanDesc: BeanDescription) = {
    if (!JVALUE.isAssignableFrom(theType.getRawClass)) null
    else new JValueSerializer
  }

}
