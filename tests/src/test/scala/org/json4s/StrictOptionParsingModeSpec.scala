package org.json4s

import org.specs2.mutable.Specification
import org.json4s.native.Document

class NativeStrictOptionParsingModeSpec extends StrictOptionParsingModeSpec[Document]("Native") with native.JsonMethods
class JacksonStrictOptionParsingModeSpec extends StrictOptionParsingModeSpec[JValue]("Jackson") with jackson.JsonMethods

abstract class StrictOptionParsingModeSpec[T](mod: String) extends Specification with JsonMethods[T] {

  implicit lazy val formats = new DefaultFormats { override val strictOptionParsing = true }

  val doubleForIntJson  = """{ "someDouble": 10.0, "someString": "abc", "someInt": 10.0, "someMap": {}, "someBoolean": true }"""
  val booleanForIntJson = """{ "someDouble": 10.0, "someString": "abc", "someInt": true, "someMap": {}, "someBoolean": true }"""
  val stringForIntJson  = """{ "someDouble": 10.0, "someString": "abc", "someInt": "10", "someMap": {}, "someBoolean": true }"""
  val mapForIntJson     = """{ "someDouble": 10.0, "someString": "abc", "someInt": {}, "someMap": {}, "someBoolean": true }"""

  val intForDoubleJson     = """{ "someDouble": 10, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val booleanForDoubleJson = """{ "someDouble": true, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val stringForDoubleJson  = """{ "someDouble": "10.0", "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val mapForDoubleJson     = """{ "someDouble": {}, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""

  val intForBooleanJson    = """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": 10 }"""
  val doubleForBooleanJson = """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": 10.0 }"""
  val stringForBooleanJson = """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": "true" }"""
  val mapForBooleanJson    = """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": {} }"""

  val doubleForStringJson  = """{ "someDouble": 10.0, "someString": 10.0, "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val intForStringJson     = """{ "someDouble": 10.0, "someString": 10, "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val booleanForStringJson = """{ "someDouble": 10.0, "someString": false, "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val mapForStringJson     = """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": {}, "someBoolean": true }"""

  val intForMapJson     = """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": 10, "someBoolean": true }"""
  val doubleForMapJson  = """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": 10.0, "someBoolean": true }"""
  val booleanForMapJson = """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": true, "someBoolean": true }"""
  val stringForMapJson  = """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": "some string", "someBoolean": true }"""

  val correctJson = """{ "someDouble": 10.0, "someString": "someString", "someInt": 10, "someMap": {}, "someBoolean": true }"""

  (mod + " case class with optional values in strict mode") should {
    "throw an error on parsing a string for an int" in {
      (parse(stringForIntJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a boolean for an int" in {
      (parse(booleanForIntJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a map for an int" in {
      (parse(mapForIntJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "parse double as an int" in {
      val model = parse(doubleForIntJson).extract[OptionalValueModel]
      model.someInt must_== Some(10)
    }

    "throw an error on parsing a string for a double" in {
      (parse(stringForDoubleJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a boolean for a double" in {
      (parse(booleanForDoubleJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a map for a double" in {
      (parse(mapForDoubleJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "parse int as a double" in {
      val model = parse(intForDoubleJson).extract[OptionalValueModel]
      model.someInt must_== Some(10.0)
    }

    "throw an error on parsing a int for a boolean" in {
      (parse(intForBooleanJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a double for a boolean" in {
      (parse(doubleForBooleanJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a string for a boolean" in {
      (parse(stringForBooleanJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a map for a boolean" in {
      (parse(mapForBooleanJson).extract[OptionalValueModel]) must throwA[MappingException]
    }

    "throw an error on parsing a boolean for a string" in {
      (parse(booleanForStringJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a map for a string" in {
      (parse(mapForStringJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "parse int as a string" in {
      val model = parse(intForStringJson).extract[OptionalValueModel]
      model.someString must_== Some("10")
    }
    "parse double as a string" in {
      val model = parse(doubleForStringJson).extract[OptionalValueModel]
      model.someString must_== Some("10.0")
    }

    "throw an error on parsing a int for a map" in {
      (parse(intForMapJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a double for a map" in {
      (parse(doubleForMapJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a string for a map" in {
      (parse(stringForMapJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on parsing a boolean for a map" in {
      (parse(booleanForMapJson).extract[OptionalValueModel]) must throwA[MappingException]
    }

    "extract the class if all values are correctly typed" in {
      val model = parse(correctJson).extract[OptionalValueModel]
      model.someDouble must_== Some(10.0)
      model.someInt must_== Some(10)
      model.someString must_== Some("someString")
      model.someMap must_== Some(Map[String,Any]())
      model.someBoolean must_== Some(true)
    }
  }
}

case class OptionalValueModel(
  val someInt: Option[Int],
  val someDouble: Option[Double],
  val someString: Option[String],
  val someMap: Option[Map[String, Any]],
  val someBoolean: Option[Boolean]
)

