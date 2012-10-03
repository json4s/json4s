package org.json4s

import org.specs2.mutable.Specification
import text.Document

object NativeExtractionBugs extends ExtractionBugs[Document]("Native") with native.JsonMethods
object JacksonExtractionBugs extends ExtractionBugs[JValue]("Native") with jackson.JsonMethods

object ExtractionBugs {
  case class Response(data: List[Map[String, Int]])

  case class OptionOfInt(opt: Option[Int])

  case class PMap(m: Map[String, List[String]])

  case class ManyConstructors(id: Long, name: String, lastName: String, email: String) {
    def this() = this(0, "John", "Doe", "")
    def this(name: String) = this(0, name, "Doe", "")
    def this(name: String, email: String) = this(0, name, "Doe", email)
  }

  case class ExtractWithAnyRef()

  case class UnicodeFieldNames(`foo.bar,baz`: String)

  object HasCompanion {
    def hello = "hello"
  }
  case class HasCompanion(nums: List[Int])
}
abstract class ExtractionBugs[T](mod: String) extends Specification with JsonMethods[T] {
  title(mod+" Extraction bugs Specification")

  import ExtractionBugs._
  implicit val formats = DefaultFormats

  "ClassCastException (BigInt) regression 2 must pass" in {
    val opt = OptionOfInt(Some(39))
    Extraction.decompose(opt).extract[OptionOfInt].opt.get mustEqual 39
  }

  "Extraction should not fail when Maps values are Lists" in {
    val m = PMap(Map("a" -> List("b"), "c" -> List("d")))
    Extraction.decompose(m).extract[PMap] mustEqual m
  }

  "Extraction should always choose constructor with the most arguments if more than one constructor exists" in {
    val args = Meta.Reflection.primaryConstructorArgs(classOf[ManyConstructors])
    args.size mustEqual 4
  }

  "Extraction should handle AnyRef" in {
    implicit val formats = DefaultFormats.withHints(FullTypeHints(classOf[ExtractWithAnyRef] :: Nil))
    val json = JObject(JField("jsonClass", JString(classOf[ExtractWithAnyRef].getName)) :: Nil)
    val extracted = Extraction.extract[AnyRef](json)
    extracted mustEqual ExtractWithAnyRef()
  }

  "Extraction should work with unicode encoded field names (issue 1075)" in {
    parseJson("""{"foo.bar,baz":"x"}""").extract[UnicodeFieldNames] mustEqual UnicodeFieldNames("x")
  }

  "Extraction should not fail if case class has a companion object" in {
    parseJson("""{"nums":[10]}""").extract[HasCompanion] mustEqual HasCompanion(List(10))
  }

  "Issue 1169" in {
    val json = parseJson("""{"data":[{"one":1, "two":2}]}""")
    json.extract[Response] mustEqual Response(List(Map("one" -> 1, "two" -> 2)))
  }


}

