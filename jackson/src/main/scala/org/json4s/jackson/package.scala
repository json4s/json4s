package org.json4s

import com.fasterxml.jackson.databind.ObjectMapper

package object jackson {
  def parseJson(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue =
    JsonMethods.parse(in, useBigDecimalForDouble)
  def parseJsonOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue] =
    JsonMethods.parseOpt(in, useBigDecimalForDouble)

  def renderJValue(value: JValue): JValue = JsonMethods.render(value)
  def compactJson(d: JValue): String = JsonMethods.compact(d)
  def prettyJson(d: JValue): String = JsonMethods.pretty(d)
}