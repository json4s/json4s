package org.json4s

import org.specs2.mutable.Specification
import org.json4s.native.Document

object NativeUseCompanionCtorSpec extends UseCompanionConstructors[Document]("Native") with native.JsonMethods
object JacksonUseCompanionCtorSpec extends UseCompanionConstructors[JValue]("Jackson") with jackson.JsonMethods

final case class UseCompanionCtorSpec(someString: String, someInt: Int, someDouble: Double)

object UseCompanionCtorSpec {
  def apply(someString: String, someInt: Int): UseCompanionCtorSpec = UseCompanionCtorSpec(someString, someInt, 1.1D)
}

abstract class UseCompanionConstructors[T](mod: String) extends Specification with JsonMethods[T] {
  
  implicit lazy val formats = DefaultFormats

  val useCtorSpec1 = """{ "someString": "Foo", "someInt": 123 }"""
  val useCtorSpec2 = """{ "someString": "Foo", "someInt": 123, "someDouble": 456.1 }"""

  (mod + " case class with a companion ctor") should {
    "use companion ctor" in {
      val model = parse(useCtorSpec1).extract[UseCompanionCtorSpec]
      model.someString must_== "Foo"
      model.someInt must_== 123
      model.someDouble must_== 1.1
    }
    "use primary ctor" in {
      val model = parse(useCtorSpec2).extract[UseCompanionCtorSpec]
      model.someString must_== "Foo"
      model.someInt must_== 123
      model.someDouble must_== 456.1
    }
  }
}
