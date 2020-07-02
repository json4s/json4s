package org.json4s
import org.json4s.jackson.{JsonMethods, Serialization}
import org.specs2.mutable.Specification

class TypeHintTest extends Specification {

  import TypeHintsTest._

  "deserialization with type hints" should {

    implicit val formats: Formats = DefaultFormats + MappedTypeHints(Map(classOf[Foo] -> "foo", classOf[Bar] -> "bar"))

    "fail when the type hint is incompatible with the requested type" in {
      val dump = Serialization.write(Foo(1))
      JsonMethods.parse(dump).extract[Bar] must throwA[MappingException]
    }

    "succeed when the type hint is compatible with the requested type" in {
      val dump = Serialization.write(Foo(1))
      JsonMethods.parse(dump).extract[MyTrait] shouldEqual Foo(1)
      JsonMethods.parse(dump).extract[Foo] shouldEqual Foo(1)
    }
  }
}

object TypeHintsTest {
  trait MyTrait
  case class Foo(foo: Int) extends MyTrait
  case class Bar(bar: String) extends MyTrait
}
