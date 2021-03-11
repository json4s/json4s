package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.Document

sealed trait Item
case class Rock() extends Item

object Music {
  sealed trait Genre
  case class Rock() extends Genre
}

class NativeJsonFormatsSpec extends JsonFormatsSpec[Document]("Native") with native.JsonMethods

/**
 * System under specification for JSON Formats.
 */
abstract class JsonFormatsSpec[T](mod: String) extends AnyWordSpec with TypeHintExamples with JsonMethods[T] {

  implicit val formats: Formats = ShortTypeHintExamples.formats + FullTypeHintExamples.formats.typeHints

  val hintsForFish = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Fish]).get
  val hintsForDog = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Dog]).get
  val hintsForAnimal = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Animal]).get

  (mod + " JsonFormats Specification") should {
    "hintsFor across composite formats" in {
      assert(formats.typeHints.hintFor(classOf[Fish]) == Some(hintsForFish))
      assert(formats.typeHints.hintFor(classOf[Dog]) == Some(hintsForDog))
      assert(formats.typeHints.hintFor(classOf[Animal]) == Some(hintsForAnimal))
    }

    "classFor across composite formats" in {
      assert(
        formats.typeHints.classFor(hintsForFish, classOf[Animal]) == (FullTypeHintExamples.formats.typeHints
          .classFor(hintsForFish, classOf[Animal]))
      )
      assert(
        formats.typeHints.classFor(hintsForDog, classOf[Animal]) == (FullTypeHintExamples.formats.typeHints
          .classFor(hintsForDog, classOf[Animal]))
      )
      assert(
        formats.typeHints.classFor(hintsForAnimal, classOf[Animal]) == (FullTypeHintExamples.formats.typeHints
          .classFor(hintsForAnimal, classOf[Animal]))
      )
    }

    "parameter name reading strategy can be changed" in {
      object TestReader extends ParameterNameReader {
        def lookupParameterNames(constructor: reflect.Executable) = List("name", "age")
      }
      implicit val formats: Formats = new DefaultFormats { override val parameterNameReader = TestReader }
      val json = parse("""{"name":"joe","age":35}""")
      assert(json.extract[NamesNotSameAsInJson] == NamesNotSameAsInJson("joe", 35))
    }

    "Duplicate hints for orthogonal classes should not interfere with each other" in {
      implicit val formats: Formats = native.Serialization.formats(NoTypeHints) +
        MappedTypeHints(Map(classOf[Rock] -> "rock")) +
        MappedTypeHints(Map(classOf[Music.Rock] -> "rock"))
      val json = parse("""{"jsonClass": "rock"}""")
      assert(json.extract[Item] == Rock())
      assert(json.extract[Music.Genre] == Music.Rock())
    }

    "Unicode escaping can be changed" should {
      val json = parse("""{"Script Small G": "\u210A"}""")

      "escaped" in {
        assert(compact(render(json, alwaysEscapeUnicode = true)) == "{\"Script Small G\":\"\\u210A\"}")
      }

      "not escaped" in {
        assert(compact(render(json, alwaysEscapeUnicode = false)) == "{\"Script Small G\":\"\u210A\"}")
      }
    }
  }
}

case class NamesNotSameAsInJson(n: String, a: Int)
