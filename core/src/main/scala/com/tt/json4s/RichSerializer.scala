package com.tt.json4s

import com.tt.json4s.reflect.ScalaType

trait RichSerializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), A]
  def serialize(implicit format: Formats): PartialFunction[Any, JValue]
}
