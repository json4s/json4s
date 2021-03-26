package org.json4s

import org.scalacheck.Arbitrary
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.Checkers
import scala.util.Try

/**
 * System under specification for JSON Printing.
 */
class JsonPrintingSpec extends AnyWordSpec with JValueGen with Checkers {
  import native.Document
  import native.Printer
  import native.JsonMethods._

  "rendering does not change semantics" in check { (json: Document) =>
    parse(Printer.pretty(json)) == parse(Printer.compact(json))
  }

  private def parse(json: String) = {
    Try { native.JsonMethods.parse(json) }.toOption
  }

  implicit def arbDoc: Arbitrary[Document] = Arbitrary(genJValue.map(render(_)))
}
