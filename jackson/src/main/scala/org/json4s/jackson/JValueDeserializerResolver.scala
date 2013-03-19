package org.json4s
package jackson

import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.{BeanDescription, DeserializationConfig, JavaType}

private object JValueDeserializerResolver extends Deserializers.Base {
  private val JVALUE = classOf[JValue]

  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription) = {
    if (!JVALUE.isAssignableFrom(javaType.getRawClass)) null
    else new JValueDeserializer(config.getTypeFactory(), javaType.getRawClass)
  }
}
