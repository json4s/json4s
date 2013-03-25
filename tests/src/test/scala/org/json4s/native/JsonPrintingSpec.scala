package org.json4s

import org.specs2.ScalaCheck
import org.scalacheck.Arbitrary
import org.specs2.mutable.Specification


/**
* System under specification for JSON Printing.
*/
object JsonPrintingSpec extends Specification with JValueGen with ScalaCheck {
  import scala.text.Document
  import native.Printer
  import native.JsonMethods._

  "rendering does not change semantics" in {
    val rendering = (json: Document) => parse(Printer.pretty(json)) must_== parse(Printer.compact(json))
    prop(rendering)
  }

  private def parse(json: String) = scala.util.parsing.json.JSON.parseFull(json)

  implicit def arbDoc: Arbitrary[Document] = Arbitrary(genJValue.map(render(_)))
}

