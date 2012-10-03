package org.json4s


import org.specs2.mutable.Specification
import text.Document

object NativeJsonFormatsSpec extends JsonFormatsSpec[Document]("Native") with native.JsonMethods
object JacksonJsonFormatsSpec extends JsonFormatsSpec[JValue]("Jackson") with jackson.JsonMethods
/**
* System under specification for JSON Formats.
*/
abstract class JsonFormatsSpec[T](mod: String) extends Specification with TypeHintExamples with JsonMethods[T] {
  implicit val formats = ShortTypeHintExamples.formats + FullTypeHintExamples.formats.typeHints

  val hintsForFish   = ShortTypeHintExamples.formats.typeHints.hintFor(classOf[Fish])
  val hintsForDog    = ShortTypeHintExamples.formats.typeHints.hintFor(classOf[Dog])
  val hintsForAnimal = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Animal])

  (mod+" JsonFormats Specification") in {
    "hintsFor across composite formats" in {
      formats.typeHints.hintFor(classOf[Fish])   mustEqual (hintsForFish)
      formats.typeHints.hintFor(classOf[Dog])    mustEqual (hintsForDog)
      formats.typeHints.hintFor(classOf[Animal]) mustEqual (hintsForAnimal)
    }

    "classFor across composite formats" in {
      formats.typeHints.classFor(hintsForFish)   mustEqual (ShortTypeHintExamples.formats.typeHints.classFor(hintsForFish))
      formats.typeHints.classFor(hintsForDog)    mustEqual (ShortTypeHintExamples.formats.typeHints.classFor(hintsForDog))
      formats.typeHints.classFor(hintsForAnimal) mustEqual (FullTypeHintExamples.formats.typeHints.classFor(hintsForAnimal))
    }

    "parameter name reading strategy can be changed" in {
      object TestReader extends ParameterNameReader {
        def lookupParameterNames(constructor: java.lang.reflect.Constructor[_]) = List("name", "age")
      }
      implicit val formats = new DefaultFormats { override val parameterNameReader = TestReader }
      val json = parseJson("""{"name":"joe","age":35}""")
      json.extract[NamesNotSameAsInJson] mustEqual NamesNotSameAsInJson("joe", 35)
    }
  }
}

case class NamesNotSameAsInJson(n: String, a: Int)
