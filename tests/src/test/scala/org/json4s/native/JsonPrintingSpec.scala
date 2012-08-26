package org.json4s

import org.specs.{ScalaCheck, Specification}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll


/**
* System under specification for JSON Printing.
*/
object JsonPrintingSpec extends Specification("JSON Printing Specification") with JValueGen with ScalaCheck {
  import scala.text.Document
  import native.Printer
  import native.JsonMethods._

  "rendering does not change semantics" in {
    val rendering = (json: Document) => parse(Printer.pretty(json)) == parse(Printer.compact(json))
    forAll(rendering) must pass
  }

  private def parse(json: String) = scala.util.parsing.json.JSON.parse(json)

  implicit def arbDoc: Arbitrary[Document] = Arbitrary(genJValue.map(render(_).asInstanceOf[Document]))
}

