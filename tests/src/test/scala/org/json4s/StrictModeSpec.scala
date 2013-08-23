package org.json4s

import org.specs2.mutable.Specification
import text.Document

object NativeStrictModeSpec extends StrictModeSpec[Document]("Native") with native.JsonMethods
object JacksonStrictModeSpec extends StrictModeSpec[JValue]("Jackson") with jackson.JsonMethods

abstract class StrictModeSpec[T](mod: String) extends Specification with JsonMethods[T] {
  
  implicit lazy val formats = new DefaultFormats { override val strict = true }
  
  val wrongIntJson = """{ "someDouble": 10.0, "someString": "abc", "someInt": "I'm not an int" }"""
  val wrongDoubleJson = """{ "someDouble": "I'm not a double", "someString": "abc", "someInt": 10 }"""
  val wrongStringJson = """{ "someDouble": 10.0, "someString": 10, "someInt": 10 }"""
  val correctJson = """{ "someDouble": 10.0, "someString": "someString", "someInt": 10 }"""
    
  (mod + " case class with optional values in strict mode") should {
    "throw an error on an incorrect int" in {
      (parse(wrongIntJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on an incorrect double" in {
      (parse(wrongDoubleJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "throw an error on an incorrect string" in {
      (parse(wrongStringJson).extract[OptionalValueModel]) must throwA[MappingException]
    }
    "extract the class if all values are correctly typed" in {
      val model = parse(correctJson).extract[OptionalValueModel]
      model.someDouble must_== 10.0
      model.someInt must_== 10
      model.someString must_== "someString"
    }
  }
}

case class OptionalValueModel(
  val someInt: Option[Int],
  val someDouble: Option[Double],
  val someString: Option[String]
)

