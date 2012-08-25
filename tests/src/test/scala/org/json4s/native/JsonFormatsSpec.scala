package org.json4s
package native

import org.specs.Specification
import JsonMethods._

/**
 * System under specification for JSON Formats.
 */
object JsonFormatsSpec extends Specification("JsonFormats Specification") with TypeHintExamples {
  implicit val formats = ShortTypeHintExamples.formats + FullTypeHintExamples.formats.typeHints

  val hintsForFish   = ShortTypeHintExamples.formats.typeHints.hintFor(classOf[Fish])
  val hintsForDog    = ShortTypeHintExamples.formats.typeHints.hintFor(classOf[Dog])
  val hintsForAnimal = FullTypeHintExamples.formats.typeHints.hintFor(classOf[Animal])

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
    val json = parse("""{"name":"joe","age":35}""")
    json.extract[NamesNotSameAsInJson] mustEqual NamesNotSameAsInJson("joe", 35)
  }
}

case class NamesNotSameAsInJson(n: String, a: Int)
