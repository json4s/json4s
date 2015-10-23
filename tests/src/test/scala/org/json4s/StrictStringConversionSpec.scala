package org.json4s

import org.specs2.mutable.Specification

import scala.text.Document

object NativeStrictStringConversionSpec extends StrictStringConversionSpec[Document]("Native") with native.JsonMethods
object JacksonStrictStringConversionSpec extends StrictStringConversionSpec[JValue]("Jackson") with jackson.JsonMethods

abstract class StrictStringConversionSpec[T](mod: String) extends Specification with JsonMethods[T] {

  implicit lazy val formats = new DefaultFormats { override val strictStringConversion = true }

  (mod + " case class with string element in strict mode") should {
    "throw an error on parsing a JInt as that string" in {
      (JObject(JField("str", JInt(1))).extract[BasicStrModel]) must throwA[MappingException]
    }

    "throw an error on parsing a JLong as that string" in {
      (JObject(JField("str", JLong(1))).extract[BasicStrModel]) must throwA[MappingException]
    }

    "throw an error on parsing a JDouble as that string" in {
      (JObject(JField("str", JDouble(1))).extract[BasicStrModel]) must throwA[MappingException]
    }

    "throw an error on parsing a JDecimal as that string" in {
      (JObject(JField("str", JDecimal(1))).extract[BasicStrModel]) must throwA[MappingException]
    }

    "throw an error on parsing a JInt as that string" in {
      val model = JObject(JField("str", JString("test"))).extract[BasicStrModel]
      model.str must_== "test"
    }
  }
}

case class BasicStrModel(str: String)