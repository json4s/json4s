package org.json4s

import org.json4s.jackson.{JsonMethods, Serialization}
import org.scalatest.wordspec.AnyWordSpec

class TypeHintTest extends AnyWordSpec {

  import TypeHintsTest._

  "deserialization with type hints" should {

    implicit val formats: Formats = DefaultFormats + CustomTuple2Serializer + MappedTypeHints(
      Map(classOf[Foo] -> "foo", classOf[Bar] -> "bar", classOf[Baz] -> "baz")
    )

    "fail when the type hint is incompatible with the requested type" in {
      val dump = Serialization.write(Foo(1))
      // : MyTrait is important for reproducing the behavior reported in https://github.com/json4s/json4s/issues/617
      assertThrows[MappingException] {
        (JsonMethods.parse(dump).extract[Bar]: MyTrait)
      }
    }

    "fail when the type hint of a nested field is incompatible with the requested type" in {
      val json = """{"t": [{"baz": "a string", "jsonClass": "baz"}, 2]}"""
      assertThrows[MappingException] {
        JsonMethods.parse(json).extract[Container]
      }
    }

    "succeed when the type hint is compatible with the requested type" in {
      val dump = Serialization.write(Foo(1))
      assert(JsonMethods.parse(dump).extract[MyTrait] == Foo(1))
      assert(JsonMethods.parse(dump).extract[Foo] == Foo(1))
    }
  }
}

object TypeHintsTest {
  trait MyTrait
  case class Foo(foo: Int) extends MyTrait
  case class Bar(bar: String) extends MyTrait
  case class Baz(baz: String)
  case class Container(t: (MyTrait, Int))
}
