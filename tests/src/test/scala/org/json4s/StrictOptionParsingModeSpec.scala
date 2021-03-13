package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.Document

class NativeStrictOptionParsingModeSpec extends StrictOptionParsingModeSpec[Document]("Native") with native.JsonMethods
class JacksonStrictOptionParsingModeSpec extends StrictOptionParsingModeSpec[JValue]("Jackson") with jackson.JsonMethods

abstract class StrictOptionParsingModeSpec[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  implicit lazy val formats: Formats = new DefaultFormats { override val strictOptionParsing = true }

  val doubleForIntJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": 10.0, "someMap": {}, "someBoolean": true }"""
  val booleanForIntJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": true, "someMap": {}, "someBoolean": true }"""
  val stringForIntJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": "10", "someMap": {}, "someBoolean": true }"""
  val mapForIntJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": {}, "someMap": {}, "someBoolean": true }"""

  val intForDoubleJson =
    """{ "someDouble": 10, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val booleanForDoubleJson =
    """{ "someDouble": true, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val stringForDoubleJson =
    """{ "someDouble": "10.0", "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val mapForDoubleJson =
    """{ "someDouble": {}, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": true }"""

  val intForBooleanJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": 10 }"""
  val doubleForBooleanJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": 10.0 }"""
  val stringForBooleanJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": "true" }"""
  val mapForBooleanJson =
    """{ "someDouble": 10.0, "someString": "abc", "someInt": 10, "someMap": {}, "someBoolean": {} }"""

  val doubleForStringJson =
    """{ "someDouble": 10.0, "someString": 10.0, "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val intForStringJson =
    """{ "someDouble": 10.0, "someString": 10, "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val booleanForStringJson =
    """{ "someDouble": 10.0, "someString": false, "someInt": 10, "someMap": {}, "someBoolean": true }"""
  val mapForStringJson =
    """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": {}, "someBoolean": true }"""

  val intForMapJson = """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": 10, "someBoolean": true }"""
  val doubleForMapJson =
    """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": 10.0, "someBoolean": true }"""
  val booleanForMapJson =
    """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": true, "someBoolean": true }"""
  val stringForMapJson =
    """{ "someDouble": 10.0, "someString": {}, "someInt": 10, "someMap": "some string", "someBoolean": true }"""

  val correctJson =
    """{ "someDouble": 10.0, "someString": "someString", "someInt": 10, "someMap": {}, "someBoolean": true }"""

  (mod + " case class with optional values in strict mode") should {
    "throw an error on parsing a string for an int" in {
      assertThrows[MappingException] { (parse(stringForIntJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a boolean for an int" in {
      assertThrows[MappingException] { (parse(booleanForIntJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a map for an int" in {
      assertThrows[MappingException] { (parse(mapForIntJson).extract[OptionalValueModel]) }
    }
    "parse double as an int" in {
      val model = parse(doubleForIntJson).extract[OptionalValueModel]
      assert(model.someInt == Some(10))
    }

    "throw an error on parsing a string for a double" in {
      assertThrows[MappingException] { (parse(stringForDoubleJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a boolean for a double" in {
      assertThrows[MappingException] { (parse(booleanForDoubleJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a map for a double" in {
      assertThrows[MappingException] { (parse(mapForDoubleJson).extract[OptionalValueModel]) }
    }
    "parse int as a double" in {
      val model = parse(intForDoubleJson).extract[OptionalValueModel]
      assert(model.someInt == Some(10.0))
    }

    "throw an error on parsing a int for a boolean" in {
      assertThrows[MappingException] { (parse(intForBooleanJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a double for a boolean" in {
      assertThrows[MappingException] { (parse(doubleForBooleanJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a string for a boolean" in {
      assertThrows[MappingException] { (parse(stringForBooleanJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a map for a boolean" in {
      assertThrows[MappingException] { (parse(mapForBooleanJson).extract[OptionalValueModel]) }
    }

    "throw an error on parsing a boolean for a string" in {
      assertThrows[MappingException] { (parse(booleanForStringJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a map for a string" in {
      assertThrows[MappingException] { (parse(mapForStringJson).extract[OptionalValueModel]) }
    }
    "parse int as a string" in {
      val model = parse(intForStringJson).extract[OptionalValueModel]
      assert(model.someString == Some("10"))
    }
    "parse double as a string" in {
      val model = parse(doubleForStringJson).extract[OptionalValueModel]
      assert(model.someString == Some("10.0"))
    }

    "throw an error on parsing a int for a map" in {
      assertThrows[MappingException] { (parse(intForMapJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a double for a map" in {
      assertThrows[MappingException] { (parse(doubleForMapJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a string for a map" in {
      assertThrows[MappingException] { (parse(stringForMapJson).extract[OptionalValueModel]) }
    }
    "throw an error on parsing a boolean for a map" in {
      assertThrows[MappingException] { (parse(booleanForMapJson).extract[OptionalValueModel]) }
    }

    "extract the class if all values are correctly typed" in {
      val model = parse(correctJson).extract[OptionalValueModel]
      assert(model.someDouble == Some(10.0))
      assert(model.someInt == Some(10))
      assert(model.someString == Some("someString"))
      assert(model.someMap == Some(Map[String, Any]()))
      assert(model.someBoolean == Some(true))
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
