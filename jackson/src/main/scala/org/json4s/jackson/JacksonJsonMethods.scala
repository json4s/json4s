package org.json4s
package jackson

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}

trait JacksonJsonMethods extends JsonMethods[JValue] {

  private val _defaultMapper = new ObjectMapper()
  def mapper = _defaultMapper
  mapper.registerModule(Json4sScalaModule)
  mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)


  def parse(s: String): _root_.org.json4s.JValue = {
    mapper.readValue[JValue](s, classOf[JValue])
  }

  def parseOpt(s: String): Option[_root_.org.json4s.JValue] = try {
    Option(parse(s))
  } catch { case _: Throwable => None}

  def render(value: _root_.org.json4s.JValue): _root_.org.json4s.JValue = value

  def compact(d: _root_.org.json4s.JValue): String = mapper.writeValueAsString(d)

  def pretty(d: _root_.org.json4s.JValue): String = {
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }
}

object JacksonJsonMethods extends JacksonJsonMethods
