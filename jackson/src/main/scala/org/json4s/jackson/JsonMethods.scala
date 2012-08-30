package org.json4s
package jackson

import com.fasterxml.jackson.databind.ObjectMapper
import util.control.Exception.allCatch
import org.json4s

trait JsonMethods extends json4s.JsonMethods[JValue] {

  private lazy val _defaultMapper = {
    val m = new ObjectMapper()
    m.registerModule(new Json4sScalaModule)
    m
  }
  def mapper = _defaultMapper



  def parse(s: String): JValue = {
    mapper.readValue[JValue](s, classOf[JValue])
  }

  def parseOpt(s: String): Option[JValue] = try {
    Option(parse(s))
  } catch { case _: Throwable => None}

  def render(value: JValue): JValue = value

  def compact(d: JValue): String = mapper.writeValueAsString(d)

  def pretty(d: JValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }



}


//class JValueExt(jv: JValue) {
//  def extract[T:Manifest:Reader]: T = implicitly[Reader[T]].read(jv)
//  def extractOpt[T:Manifest:Reader]: Option[T] = allCatch.withApply(_ => None) { Option(extract) }
//  def extractOrElse[T:Manifest:Reader](default: => T) = extractOpt getOrElse default
//}
object JsonMethods extends JsonMethods
