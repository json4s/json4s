package org.json4s

package object jackson {
  def parseJson[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false): JValue =
    JsonMethods.parse(in, useBigDecimalForDouble)

  def parseJsonOpt[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false): Option[JValue] =
    JsonMethods.parseOpt(in, useBigDecimalForDouble)

  def renderJValue(value: JValue)(implicit formats: Formats = DefaultFormats): JValue =
    JsonMethods.render(
      value = value,
      alwaysEscapeUnicode = formats.alwaysEscapeUnicode,
      emptyValueStrategy = formats.emptyValueStrategy
    )

  def compactJson(d: JValue): String = JsonMethods.compact(d)

  def prettyJson(d: JValue): String = JsonMethods.pretty(d)
}
