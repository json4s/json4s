package org.json4s
package jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.Deserializers

private object JValueDeserializerResolver extends Deserializers.Base {
  private[this] val J_VALUE = classOf[JValue]

  override def findBeanDeserializer(
    javaType: JavaType,
    config: DeserializationConfig,
    beanDesc: BeanDescription
  ): JsonDeserializer[?] = {
    if (!J_VALUE.isAssignableFrom(javaType.getRawClass)) null
    else new JValueDeserializer(javaType.getRawClass)
  }
}
