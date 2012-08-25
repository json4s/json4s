package org.json4s
package native

import org.specs.{ScalaCheck, Specification}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import JsonMethods._


/**
 * System under specification for JSON Printing.
 */
object JsonPrintingSpec extends Specification("JSON Printing Specification") with JValueGen with ScalaCheck {
  import scala.text.Document

  "rendering does not change semantics" in {
    val rendering = (json: Document) => parse(Printer.pretty(json)) == parse(Printer.compact(json))
    forAll(rendering) must pass
  }

  private def parse(json: String) = scala.util.parsing.json.JSON.parse(json)

  implicit def arbDoc: Arbitrary[Document] = Arbitrary(genJValue.map(render(_)))
}

