package org.json4s
package jackson

import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.{BeanDescription, DeserializationConfig, JavaType}

private object JValueDeserializerResolver extends Deserializers.Base {
  private[this] val J_VALUE = classOf[JValue]

  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription) = {
    if (!J_VALUE.isAssignableFrom(javaType.getRawClass)) null
    else new JValueDeserializer(javaType.getRawClass)
  }
}
