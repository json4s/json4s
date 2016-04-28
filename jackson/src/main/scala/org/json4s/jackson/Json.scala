package org.json4s
package jackson

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.{Writer => JWriter}

object Json {
  private class UtilMethods(override val mapper: ObjectMapper) extends JsonMethods
  def apply(fmts: Formats, mapper: ObjectMapper = JsonMethods.mapper) = new Json(fmts, mapper)

}
class Json(fmts: Formats, mapper: ObjectMapper = JsonMethods.mapper) extends JsonUtil(fmts) {
  private[this] val meth: JsonMethods = new Json.UtilMethods(mapper)

  def write[A <: AnyRef : Manifest](a: A): String = mapper.writeValueAsString(decompose(a))
  def write[A <: AnyRef : Manifest, W <: JWriter](a: A, out: W): W = {
    mapper.writeValue(out, decompose(a))
    out
  }

  def writePretty[A <: AnyRef](a: A): String =
    mapper.writerWithDefaultPrettyPrinter.writeValueAsString(decompose(a))

  def writePretty[A <: AnyRef, W <: JWriter](a: A, out: W): W = {
    mapper.writerWithDefaultPrettyPrinter.writeValue(out, decompose(a))
    out
  }

  def parse(json: JsonInput): JValue = meth.parse(json, fmts.wantsBigDecimal, fmts.wantsBigInt)
  def parseOpt(json: JsonInput): Option[JValue] = meth.parseOpt(json, fmts.wantsBigDecimal, fmts.wantsBigInt)

  def withFormats(fmts: Formats): JsonUtil = new Json(fmts, mapper)
}
