package org.json4s

package object native {
  def parseJson[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false): JValue =
    JsonMethods.parse(in, useBigDecimalForDouble)

  def parseJsonOpt[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false): Option[JValue] =
    JsonMethods.parseOpt(in, useBigDecimalForDouble)

  def renderJValue(value: JValue)(implicit formats: Formats = DefaultFormats): Document =
    JsonMethods.render(
      value = value,
      alwaysEscapeUnicode = formats.alwaysEscapeUnicode,
      emptyValueStrategy = formats.emptyValueStrategy
    )

  def compactJson(d: Document): String = JsonMethods.compact(d)

  def prettyJson(d: Document): String = JsonMethods.pretty(d)
}
