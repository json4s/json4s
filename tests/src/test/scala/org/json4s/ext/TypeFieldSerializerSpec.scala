package org.json4s
package ext

import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification

object NativeTypeFieldSerializerSpec extends TypeFieldSerializerSpec("Native") {
  val s: Serialization = native.Serialization
}

object JacksonTypeFieldSerializerSpec extends TypeFieldSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}

abstract class TypeFieldSerializerSpec(mod: String) extends Specification with Matchers {

  def s: Serialization

  private val map = Map(
    "concrete1" -> classOf[Concrete1],
    "concrete2" -> classOf[Concrete2]
  )

  implicit lazy val formats1: Formats = s.formats(NoTypeHints) +
    new TypeFieldSerializer[BaseTrait]("type", map)


  (mod + " TypeFieldSerializer Specification") should {
    "Serialize concrete type" in {
      val x = Concrete1()
      val ser = s.write(x)
      ser shouldEqual """{"type":"concrete1"}"""
    }

    "Deserialize concrete type" in {
      val json = """{"type":"concrete2"}"""
      s.read[BaseTrait](json) must beLike { case Concrete2() => ok }
    }
  }

}

sealed trait BaseTrait
case class Concrete1() extends BaseTrait
case class Concrete2() extends BaseTrait