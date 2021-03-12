package org.json4s

import org.specs2.mutable.Specification
import org.json4s.native.Document

sealed trait Item
case class Rock() extends Item

object Music {
  sealed trait Genre
  case class Rock() extends Genre
}

class NativeJsonFormatsSpec extends JsonFormatsSpec[Document]("Native") with native.JsonMethods
class JacksonJsonFormatsSpec extends JsonFormatsSpec[JValue]("Jackson") with jackson.JsonMethods

/**
 * System under specification for JSON Formats.
 */
abstract class JsonFormatsSpec[T](mod: String) extends Specification with TypeHintExamples with JsonMethods[T] {
  // To ensure the state of the ObjectMapper is guaranteed, execute in order
  sequential

  implicit val formats: Formats = ShortTypeHintExamples.formats + FullTypeHintExamples.formats.typeHints

  val hintsForFish = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Fish]).get
  val hintsForDog = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Dog]).get
  val hintsForAnimal = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Animal]).get

  (mod + " JsonFormats Specification") should {
    "hintsFor across composite formats" in {
      formats.typeHints.hintFor(classOf[Fish]) must beSome(hintsForFish)
      formats.typeHints.hintFor(classOf[Dog]) must beSome(hintsForDog)
      formats.typeHints.hintFor(classOf[Animal]) must beSome(hintsForAnimal)
    }

    "classFor across composite formats" in {
      formats.typeHints.classFor(hintsForFish, classOf[Animal]) must_== (FullTypeHintExamples.formats.typeHints
        .classFor(hintsForFish, classOf[Animal]))
      formats.typeHints.classFor(hintsForDog, classOf[Animal]) must_== (FullTypeHintExamples.formats.typeHints
        .classFor(hintsForDog, classOf[Animal]))
      formats.typeHints.classFor(hintsForAnimal, classOf[Animal]) must_== (FullTypeHintExamples.formats.typeHints
        .classFor(hintsForAnimal, classOf[Animal]))
    }

    "parameter name reading strategy can be changed" in {
      object TestReader extends ParameterNameReader {
        def lookupParameterNames(constructor: reflect.Executable) = List("name", "age")
      }
      implicit val formats: Formats = new DefaultFormats { override val parameterNameReader = TestReader }
      val json = parse("""{"name":"joe","age":35}""")
      json.extract[NamesNotSameAsInJson] must_== NamesNotSameAsInJson("joe", 35)
    }

    "Duplicate hints for orthogonal classes should not interfere with each other" in {
      implicit val formats: Formats = SerializationExamples.formats +
        MappedTypeHints(Map(classOf[Rock] -> "rock")) +
        MappedTypeHints(Map(classOf[Music.Rock] -> "rock"))
      val json = parse("""{"jsonClass": "rock"}""")
      json.extract[Item] must_== Rock()
      json.extract[Music.Genre] must_== Music.Rock()
    }

    "Unicode escaping can be changed" in {
      val json = parse("""{"Script Small G": "\u210A"}""")

      "escaped" in {
        implicit val formats: Formats = new DefaultFormats { override def alwaysEscapeUnicode: Boolean = true }
        compact(render(json)) must_== "{\"Script Small G\":\"\\u210A\"}"
      }

      "not escaped" in {
        implicit val formats: Formats = DefaultFormats
        compact(render(json)) must_== "{\"Script Small G\":\"\u210A\"}"
      }
    }
  }
}

case class NamesNotSameAsInJson(n: String, a: Int)
