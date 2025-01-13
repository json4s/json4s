package org.json4s

import scala.reflect.{ClassTag, classTag}

class CustomKeySerializer[A: ClassTag](ser: Formats => (PartialFunction[String, A], PartialFunction[Any, String]))
    extends KeySerializer[A] {

  val Class = classTag[A].runtimeClass

  def deserialize(implicit format: Formats) = { case (TypeInfo(Class, _), json) =>
    if (ser(format)._1.isDefinedAt(json)) ser(format)._1(json)
    else throw new MappingException("Can't convert " + json + " to " + Class)
  }

  def serialize(implicit format: Formats) = ser(format)._2
}
