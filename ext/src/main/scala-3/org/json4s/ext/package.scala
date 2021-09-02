package org.json4s

package object ext {
  private[this] type Aux[A] = { type Value = A }

  private[ext] type EnumValue[A <: Enumeration] = A match {
    case Aux[a] => a
  }
}
