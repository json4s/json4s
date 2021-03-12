package org.json4s

final class SomeValue[A](val get: A) extends AnyVal {

  /**
   * @see [[https://github.com/scala/scala/pull/9343]]
   */
  def isEmpty: false = false
}
