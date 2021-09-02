package org.json4s

trait JValueParent { self: JValue.type =>
  type ValuesType[A <: JValue] = A#Values
}
