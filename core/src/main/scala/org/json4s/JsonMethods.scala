package org.json4s


trait JsonMethods[T] {
  def parse(s: String): JValue
  def parseOpt(s: String): Option[JValue]

  def render(value: JValue): T
  def compact(d: T): String
  def pretty(d: T): String
}
