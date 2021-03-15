package org.json4s

import org.json4s.reflect.ScalaType

trait RichSerializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), A]
  def serialize(implicit format: Formats): PartialFunction[Any, JValue]
}
