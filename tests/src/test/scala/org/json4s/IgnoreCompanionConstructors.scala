package org.json4s

import org.json4s
import org.specs2.mutable.Specification
import org.json4s.native.Document

object NativeIgnoreCompanionCtorSpec
  extends IgnoreCompanionConstructors[Document]("Native", native.Serialization)
  with native.JsonMethods
object JacksonIgnoreCompanionCtorSpec
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
  extends Specification
  with JsonMethods[T] {

  implicit lazy val formats: Formats = new DefaultFormats { override val considerCompanionConstructors = false }

  val ignoreCtorSpec1 = """{ "someString": "Foo", "someInt": 123 }"""
  val ignoreCtorSpec2 = """{ "someString": "Foo", "someInt": 123, "someDouble": 456.1 }"""
  val ignoreWithTypeParamSpec = """{ "someType": "Bar" }"""

  (mod + " case class with a companion ctor") should {
    "ignore companion ctor" in {
      (parse(ignoreCtorSpec1).extract[CompanionCtorSpec]) must throwA[MappingException]
    }
    "successfully parse using primary ctor" in {
      val model = parse(ignoreCtorSpec2).extract[CompanionCtorSpec]
      model.someString must_== "Foo"
      model.someInt must_== 123
      model.someDouble must_== 456.1
    }
    "#487 sucessfully round trip case class with type param" in {
      val json = ser.write(IgnoreWithTypeParamSpec[String]("Baz"))
      val model = parse(json).extract[IgnoreWithTypeParamSpec[String]]

      model.someType must_== "Baz"
      model.other must_== "Bar"
    }
  }
}
