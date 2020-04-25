package org.json4s

import org.specs2.ScalaCheck
import org.scalacheck.Arbitrary
import org.specs2.mutable.Specification

import scala.util.Try


/**
* System under specification for JSON Printing.
*/
class JsonPrintingSpec extends Specification with JValueGen with ScalaCheck {
  import native.Document
  import native.Printer
  import native.JsonMethods._

  "rendering does not change semantics" in {
    val rendering = (json: Document) => parse(Printer.pretty(json)) must_== parse(Printer.compact(json))
    prop(rendering)
  }

  private def parse(json: String) = {
    Try { native.JsonMethods.parse(json) }.toOption
  }

  implicit def arbDoc: Arbitrary[Document] = Arbitrary(genJValue.map(render(_)))
}

