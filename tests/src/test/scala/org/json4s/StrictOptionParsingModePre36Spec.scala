package org.json4s

import org.specs2.mutable.Specification
import org.json4s.native.Document

object NativeStrictOptionParsingPre36Spec extends StrictOptionParsingModePre36Spec[Document]("Native") with native.JsonMethods
object JacksonStrictOptionParsingPre36Spec extends StrictOptionParsingModePre36Spec[JValue]("Jackson") with jackson.JsonMethods

final case class Pre36Spec(someInt: Int, someString: Option[String], someEnum: Option[EnumSpec])
final case class EnumSpec(someString: String)

class EnumSpecSerializer extends CustomSerializer[EnumSpec](
  implicit formats =>
    ({
      case JString(s) =>
        s match {
        case "MI" | "mi" => EnumSpec(s)
        case _ => throw new MappingException(s"value ${s} not found")
      }
    }, {
      case x: EnumSpec =>
        JString(x.toString)
    })
)

abstract class StrictOptionParsingModePre36Spec[T](mod: String) extends Specification with JsonMethods[T] {

  implicit lazy val formats: Formats = new DefaultFormats { override val strictOptionParsingIgnoreMissing: Boolean = true } + new EnumSpecSerializer()

  val complexTypeSpec1 = """{ "someInt": 123, "someString": "Foo", "someEnum": "MI" }"""
  val complexTypeSpec3 = """{ "someInt": 123, "someString": "Foo", "someEnum": "XX" }"""
  val complexTypeSpec2 = """{ "someInt": 123 }"""

  (mod + " case class with a complex type") should {
    "succeed with some values" in {
      val model = parse(complexTypeSpec1).extract[Pre36Spec]
      model.someInt must_== 123
      model.someString must_== Some("Foo")
      model.someEnum must_== Some(EnumSpec("MI"))
    }
    "succeed with missing values" in {
      val model = parse(complexTypeSpec2).extract[Pre36Spec]
      model.someInt must_== 123
      model.someString must_== None
      model.someEnum must_== None
    }
    "fail expectedly with invalid values" in {
      parse(complexTypeSpec3).extract[Pre36Spec] must throwA[MappingException]
    }
    "behave expectedly with invalid values scenario 2" in {
      val json = parse(complexTypeSpec3)
      (json \ "someString").extractOpt[String] must_== Some("Foo")
      (json \ "someEnum").extractOpt[EnumSpec] must_== None
    }
    "succeed as expected with missing values" in {
      val json = parse(complexTypeSpec2)
      (json \ "someEnum").extractOpt[EnumSpec] must_== None
    }
  }
}
