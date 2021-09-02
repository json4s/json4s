package org.json4s

package object ext {
  private[ext] type EnumValue[A <: Enumeration] = A#Value
}
