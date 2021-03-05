package org.json4s

final class SomeValue[A](val get: A) extends AnyVal {
  def isEmpty: Boolean = false
}
