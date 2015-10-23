package org.json4s

import org.specs2.mutable.Specification

import scala.text.Document

object NativeStrictArrayCreationSpec extends StrictArrayCreationSpec[Document]("Native") with native.JsonMethods
object JacksonStrictArrayCreationSpec extends StrictArrayCreationSpec[JValue]("Jackson") with jackson.JsonMethods

abstract class StrictArrayCreationSpec[T](mod: String) extends Specification with JsonMethods[T] {

  implicit lazy val formats = new DefaultFormats { override val strictArrayCreation = true }

  val doesNotContainArray  = """{}"""
  val containsEmptyArray  = """{"array":[]}"""
  val containsNonEmptyArray  = """{"array":[1,2]}"""

  (mod + " case class with a single array element in strict mode") should {
    "throw an error on parsing an object without the required key" in {
      (parse(doesNotContainArray).extract[SingleArrayModel]) must throwA[MappingException]
    }
    "successfully parse an empty array" in {
      val model = parse(containsEmptyArray).extract[SingleArrayModel]
      model.array must_== Array()
    }
    "successfully parse a non empty array" in {
      val model = parse(containsNonEmptyArray).extract[SingleArrayModel]
      model.array must_== Array(1, 2)
    }
  }

}

case class SingleArrayModel(array: Array[Int])
