package com.tt.json4s
package native

import java.io.{Writer => JWriter}

object Json {
  def apply(fmts: Formats) = new Json(fmts)
}
class Json(fmts: Formats) extends JsonUtil(fmts) {

  def write[A <: AnyRef: Manifest](a: A): String = Serialization.write(a)
  def write[A <: AnyRef: Manifest, W <: JWriter](a: A, out: W): W = Serialization.write(a, out)

  def writePretty[A <: AnyRef](a: A): String = Serialization.writePretty(a)
  def writePretty[A <: AnyRef, W <: JWriter](a: A, out: W): W = Serialization.writePretty(a, out)

  def parse[A: AsJsonInput](json: A): JValue =
    JsonMethods.parse(json, fmts.wantsBigDecimal, fmts.wantsBigInt)
  def parseOpt[A: AsJsonInput](json: A): Option[JValue] =
    JsonMethods.parseOpt(json, fmts.wantsBigDecimal, fmts.wantsBigInt)

  def withFormats(fmts: Formats): JsonUtil = new Json(fmts)
}
