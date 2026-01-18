package org.json4s.native

import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.native.JsonMethods.compact
import org.json4s.native.JsonMethods.render
import org.scalatest.wordspec.AnyWordSpec

class CaseClassTest extends AnyWordSpec {

  "Serialization of case class" should {
    "succeed when inheriting from abstract class with ctor argument with different name then value in case class ctor" in {
      val personId = FooA(3)
      val actualJValue = Extraction.decompose(personId)(using DefaultFormats.lossless)
      println(actualJValue)
      val actualJsonString = compact(render(actualJValue))
      val expectedJsonString = """{"value":3}"""
      assert(actualJsonString == expectedJsonString)
    }

    "succeed when inheriting from abstract class with ctor argument with same name as value in case class ctor" in {
      val personId = FooB(3)
      val actualJValue = Extraction.decompose(personId)(using DefaultFormats.lossless)
      println(actualJValue)
      val actualJsonString = compact(render(actualJValue))
      val expectedJsonString = """{"value":3}"""
      println(s"actualJsonString: $actualJsonString")
      println(s"expectedJsonString: $expectedJsonString")
      assert(actualJsonString == expectedJsonString)
    }

    "succeed when no base class" in {
      val personId = FooNoSuperclass(3)
      val actualJValue = Extraction.decompose(personId)(using DefaultFormats.lossless)
      println(actualJValue)
      val actualJsonString = compact(render(actualJValue))
      val expectedJsonString = """{"value":3}"""
      assert(actualJsonString == expectedJsonString)
    }
  }
}

abstract class SuperClassA[T](someValue: T) {
  def getValue: T = someValue
}

// Extending this causes trouble, seemingly because the name of the argument is `value`
abstract class SuperClassB[T](value: T) {
  def getValue: T = value
}

case class FooA(value: Long) extends SuperClassA[Long](value)
case class FooB(value: Long) extends SuperClassB[Long](value)
case class FooNoSuperclass(value: Long)
