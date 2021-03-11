package org.json4s

import org.json4s.prefs.EmptyValueStrategy

trait JsonMethods[T] {

  def parse[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false, useBigIntForLong: Boolean = true): JValue
  def parseOpt[A: AsJsonInput](
    in: A,
    useBigDecimalForDouble: Boolean = false,
    useBigIntForLong: Boolean = true
  ): Option[JValue]

  def render(
    value: JValue,
    alwaysEscapeUnicode: Boolean = false,
    emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default
  ): T
  def compact(d: T): String
  def pretty(d: T): String
}
