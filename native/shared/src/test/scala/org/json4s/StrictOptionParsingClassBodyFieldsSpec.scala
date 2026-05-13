package org.json4s

import org.json4s.native.Document
import org.scalatest.wordspec.AnyWordSpec

class NativeStrictOptionParsingClassBodyFieldsSpec
  extends StrictOptionParsingClassBodyFieldsSpec[Document]("Native")
  with native.JsonMethods

abstract class StrictOptionParsingClassBodyFieldsSpec[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  // Default `strictOptionParsing`: also enforces that every class-body Option field
  // appears in the input JSON.
  implicit lazy val strictFormats: Formats = new DefaultFormats { override val strictOptionParsing = true }

  // strictOptionParsing with the class-body Option presence check relaxed: type-mismatch
  // strictness on extracted values is preserved, but missing class-body Option keys fall
  // back to their declared defaults (matching pre-4.0 behavior).
  lazy val relaxedFormats: Formats = strictFormats.relaxStrictOptionParsingClassBodyFields

  val onlyNameJson = """{ "name": "foo" }"""
  val nameAndIsPauseJson = """{ "name": "foo", "isPause": true }"""

  (mod + " strictOptionParsing with default class-body enforcement") should {
    "fail when a class-body Option key is missing from the JSON" in {
      assertThrows[MappingException] {
        parse(onlyNameJson).extract[ClassBodyOptionConfig](strictFormats, manifest[ClassBodyOptionConfig])
      }
    }

    "succeed when the class-body Option key is present" in {
      val parsed =
        parse(nameAndIsPauseJson).extract[ClassBodyOptionConfig](strictFormats, manifest[ClassBodyOptionConfig])
      assert(parsed.name == "foo")
    }
  }

  (mod + " strictOptionParsing with relaxStrictOptionParsingClassBodyFields") should {
    "tolerate a missing class-body Option key, falling back to the declared default" in {
      val parsed =
        parse(onlyNameJson).extract[ClassBodyOptionConfig](relaxedFormats, manifest[ClassBodyOptionConfig])
      assert(parsed.name == "foo")
      assert(parsed.isPause == None)
    }

    "still fail on type mismatches in extractOpt (strictOptionParsing semantics preserved)" in {
      // A JSON value that cannot extract into Option[Int] must still throw under strict mode,
      // even with the class-body presence check relaxed.
      val mismatchJson = """{ "opt": "not-an-int" }"""
      assertThrows[MappingException] {
        parse(mismatchJson).extract[OptIntCtorArg](relaxedFormats, manifest[OptIntCtorArg])
      }
    }
  }
}

trait HasIsPause {
  def isPause: Option[Boolean]
}

// Mirrors the trait-override pattern that breaks under 4.x strict mode: `isPause` lives in the
// class body (to satisfy the trait's abstract member) rather than the primary constructor, so
// it goes through `Extraction.setFields` rather than `buildOptionalCtorArg`.
case class ClassBodyOptionConfig(name: String) extends HasIsPause {
  override val isPause: Option[Boolean] = None
}

case class OptIntCtorArg(opt: Option[Int])
