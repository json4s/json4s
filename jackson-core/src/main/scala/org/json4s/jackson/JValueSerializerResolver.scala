package org.json4s
package jackson

import com.fasterxml.jackson.annotation.JsonFormat
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.JavaType
import tools.jackson.databind.SerializationConfig
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.ser.Serializers

private object JValueSerializerResolver extends Serializers.Base {
  private[this] val JVALUE = classOf[JValue]

  override def findSerializer(
    config: SerializationConfig,
    theType: JavaType,
    beanDescRef: BeanDescription.Supplier,
    formatOverrides: JsonFormat.Value
  ): ValueSerializer[?] = {
    if (!JVALUE.isAssignableFrom(theType.getRawClass)) null
    else new JValueSerializer
  }

}
