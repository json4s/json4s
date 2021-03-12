package org.json4s

import org.json4s
import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.Document

class NativeIgnoreCompanionCtorSpec
  extends IgnoreCompanionConstructors[Document]("Native", native.Serialization)
  with native.JsonMethods
class JacksonIgnoreCompanionCtorSpec
  extends IgnoreCompanionConstructors[JValue]("Jackson", jackson.Serialization)
  with jackson.JsonMethods

final case class CompanionCtorSpec(someString: String, someInt: Int, someDouble: Double)

final case class IgnoreWithTypeParamSpec[T](someType: T, other: String)

object CompanionCtorSpec {
  def apply(someString: String, someInt: Int): CompanionCtorSpec = CompanionCtorSpec(someString, someInt, 1.1d)
}

object IgnoreWithTypeParamSpec {
  def apply[T](someType: T): IgnoreWithTypeParamSpec[T] = IgnoreWithTypeParamSpec[T](someType, "Bar")
}

abstract class IgnoreCompanionConstructors[T](mod: String, ser: json4s.Serialization)
  extends AnyWordSpec
  with JsonMethods[T] {

  implicit lazy val formats: Formats = new DefaultFormats { override val considerCompanionConstructors = false }

  val ignoreCtorSpec1 = """{ "someString": "Foo", "someInt": 123 }"""
  val ignoreCtorSpec2 = """{ "someString": "Foo", "someInt": 123, "someDouble": 456.1 }"""
  val ignoreWithTypeParamSpec = """{ "someType": "Bar" }"""

  (mod + " case class with a companion ctor") should {
    "ignore companion ctor" in {
      assertThrows[MappingException] {
        (parse(ignoreCtorSpec1).extract[CompanionCtorSpec])
      }
    }
    "successfully parse using primary ctor" in {
      val model = parse(ignoreCtorSpec2).extract[CompanionCtorSpec]
      assert(model.someString == "Foo")
      assert(model.someInt == 123)
      assert(model.someDouble == 456.1)
    }
    "#487 sucessfully round trip case class with type param" in {
      val json = ser.write(IgnoreWithTypeParamSpec[String]("Baz"))
      val model = parse(json).extract[IgnoreWithTypeParamSpec[String]]

      assert(model.someType == "Baz")
      assert(model.other == "Bar")
    }
  }
}
