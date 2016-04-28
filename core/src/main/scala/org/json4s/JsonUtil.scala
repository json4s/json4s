package org.json4s

abstract class JsonUtil(fmts: Formats) {

  protected[this] implicit val formats: Formats = fmts

  def write[A <: AnyRef: Manifest](a: A): String
  def write[A <: AnyRef: Manifest, W <: java.io.Writer](a: A, out: W): W

  def writePretty[A <: AnyRef](a: A): String
  def writePretty[A <: AnyRef, W <: java.io.Writer](a: A, out: W): W

  def read[A: Manifest](json: JsonInput):A = parse(json).extract[A]
  def readOpt[A: Manifest](json: JsonInput): Option[A] = parseOpt(json) flatMap (_.extractOpt[A])

  def parse(json: JsonInput): JValue
  def parseOpt(json: JsonInput): Option[JValue]

  def decompose(any: Any) = Extraction.decompose(any)

  def withFormats(fmts: Formats): JsonUtil
}
