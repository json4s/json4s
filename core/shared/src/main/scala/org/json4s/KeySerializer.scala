package org.json4s

trait KeySerializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, String), A]
  def serialize(implicit format: Formats): PartialFunction[Any, String]
}
