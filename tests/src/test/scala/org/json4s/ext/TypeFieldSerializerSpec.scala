package org.json4s
package ext

import org.scalatest.wordspec.AnyWordSpec

class NativeTypeFieldSerializerSpec extends TypeFieldSerializerSpec("Native") {
  val s: Serialization = native.Serialization
}

class JacksonTypeFieldSerializerSpec extends TypeFieldSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}

abstract class TypeFieldSerializerSpec(mod: String) extends AnyWordSpec {

  def s: Serialization

  private[this] val map = Map(
    "concrete1" -> classOf[Concrete1],
    "concrete2" -> classOf[Concrete2]
  )

  implicit lazy val formats1: Formats = s.formats(NoTypeHints) +
    new TypeFieldSerializer[BaseTrait]("type", map)

  (mod + " TypeFieldSerializer Specification") should {
    "Serialize concrete type" in {
      val x = Concrete1()
      val ser = s.write(x)
      assert(ser == """{"type":"concrete1"}""")
    }

    "Deserialize concrete type" in {
      val json = """{"type":"concrete2"}"""
      assert(s.read[BaseTrait](json).isInstanceOf[Concrete2])
    }
  }

}

sealed trait BaseTrait
case class Concrete1() extends BaseTrait
case class Concrete2() extends BaseTrait
