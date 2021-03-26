package org.json4s

trait JsonMethods[T] {

  def parse[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false, useBigIntForLong: Boolean = true): JValue
  def parseOpt[A: AsJsonInput](
    in: A,
    useBigDecimalForDouble: Boolean = false,
    useBigIntForLong: Boolean = true
  ): Option[JValue]

  def render(value: JValue)(implicit formats: Formats = DefaultFormats): T
  def compact(d: T): String
  def pretty(d: T): String
}
