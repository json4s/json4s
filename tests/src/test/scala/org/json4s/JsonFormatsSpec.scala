package org.json4s


import org.specs2.mutable.Specification
import text.Document

class NativeJsonFormatsSpec extends JsonFormatsSpec[Document]("Native") with native.JsonMethods
class JacksonJsonFormatsSpec extends JsonFormatsSpec[JValue]("Jackson") with jackson.JsonMethods
/**
* System under specification for JSON Formats.
*/
abstract class JsonFormatsSpec[T](mod: String) extends Specification with TypeHintExamples with JsonMethods[T] {
  implicit val formats = ShortTypeHintExamples.formats + FullTypeHintExamples.formats.typeHints

  val hintsForFish   = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Fish])
  val hintsForDog    = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Dog])
  val hintsForAnimal = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Animal])

  (mod+" JsonFormats Specification") should {
    "hintsFor across composite formats" in {
      formats.typeHints.hintFor(classOf[Fish])   must_== (hintsForFish)
      formats.typeHints.hintFor(classOf[Dog])    must_== (hintsForDog)
      formats.typeHints.hintFor(classOf[Animal]) must_== (hintsForAnimal)
    }

    "classFor across composite formats" in {
      formats.typeHints.classFor(hintsForFish)   must_== (FullTypeHintExamples.formats.typeHints.classFor(hintsForFish))
      formats.typeHints.classFor(hintsForDog)    must_== (FullTypeHintExamples.formats.typeHints.classFor(hintsForDog))
      formats.typeHints.classFor(hintsForAnimal) must_== (FullTypeHintExamples.formats.typeHints.classFor(hintsForAnimal))
    }

    "parameter name reading strategy can be changed" in {
      object TestReader extends ParameterNameReader {
        def lookupParameterNames(constructor: java.lang.reflect.Constructor[_]) = List("name", "age")
      }
      implicit val formats = new DefaultFormats { override val parameterNameReader = TestReader }
      val json = parse("""{"name":"joe","age":35}""")
      json.extract[NamesNotSameAsInJson] must_== NamesNotSameAsInJson("joe", 35)
    }
  }
}

case class NamesNotSameAsInJson(n: String, a: Int)
