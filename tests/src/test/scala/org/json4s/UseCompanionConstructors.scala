package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.Document

class NativeUseCompanionCtorSpec extends UseCompanionConstructors[Document]("Native") with native.JsonMethods
class JacksonUseCompanionCtorSpec extends UseCompanionConstructors[JValue]("Jackson") with jackson.JsonMethods

final case class UseCompanionCtorSpec(someString: String, someInt: Int, someDouble: Double)

object UseCompanionCtorSpec {
  def apply(someString: String, someInt: Int): UseCompanionCtorSpec = UseCompanionCtorSpec(someString, someInt, 1.1d)
}

abstract class UseCompanionConstructors[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  implicit lazy val formats: Formats = DefaultFormats

  val useCtorSpec1 = """{ "someString": "Foo", "someInt": 123 }"""
  val useCtorSpec2 = """{ "someString": "Foo", "someInt": 123, "someDouble": 456.1 }"""

  (mod + " case class with a companion ctor") should {
    "use companion ctor" in {
      val model = parse(useCtorSpec1).extract[UseCompanionCtorSpec]
      assert(model.someString == "Foo")
      assert(model.someInt == 123)
      assert(model.someDouble == 1.1)
    }
    "use primary ctor" in {
      val model = parse(useCtorSpec2).extract[UseCompanionCtorSpec]
      assert(model.someString == "Foo")
      assert(model.someInt == 123)
      assert(model.someDouble == 456.1)
    }
  }
}
