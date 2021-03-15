package org.json4s

trait Serializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), A]
  def serialize(implicit format: Formats): PartialFunction[Any, JValue]
}
