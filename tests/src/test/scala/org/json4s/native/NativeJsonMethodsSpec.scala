package org.json4s
package native

import org.specs2.mutable.Specification

class NativeJsonMethodsSpec extends Specification {

  import org.json4s.JsonDSL._
  import JsonMethods._

  "JsonMethods.parse" should {

	  val stringJson = """{"number": 200}"""

    "parse StringInput and produce JInt" in {
      (parse(stringJson) \ "number") must beAnInstanceOf[JInt]
    }

    "parse ReaderInput and produce JInt" in {
      (parse(new java.io.StringReader(stringJson)) \ "number") must beAnInstanceOf[JInt]
    }

    "parse StreamInput and produce JInt" in {
      (parse(new java.io.ByteArrayInputStream(stringJson.getBytes)) \ "number") must beAnInstanceOf[JInt]
    }

    "parse StringInput and produce JLong" in {
      (parse(stringJson, useBigIntForLong = false) \ "number") must beAnInstanceOf[JLong]
    }

    "parse ReaderInput and produce JLong" in {
      (parse(new java.io.StringReader(stringJson), useBigIntForLong = false) \ "number") must beAnInstanceOf[JLong]
    }

    "parse StreamInput and produce AST using Long" in {
      (parse(new java.io.ByteArrayInputStream(stringJson.getBytes), useBigIntForLong = false) \ "number") must beAnInstanceOf[JLong]
    }
  }

  "JsonMethods.write" should {

    "produce JSON without empty fields" in {
      implicit val formats = DefaultFormats.skippingEmptyValues

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = DocCons(DocText("["), DocCons(DocCons(DocText("1"), DocCons(DocText(","), DocText("2"))), DocText("]")))
        render(seq) must_== expected
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = DocCons(DocText("{"), DocCons(DocNest(2, DocCons(DocBreak, DocCons(DocCons(DocText("\"a\":"), DocText("1")), DocCons(DocCons(DocText(","), DocBreak), DocCons(DocText("\"d\":"), DocText("2")))))), DocCons(DocBreak, DocText("}"))))
        render(map) must_== expected
      }
    }

    "produce JSON with empty fields preserved" in {
      implicit val formats = DefaultFormats.preservingEmptyValues

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = DocCons(
          DocText("["),
          DocCons(
            DocCons(
              DocCons(
                DocCons(
                  DocText("1"),
                  DocCons(DocText(","),
                    DocText("null"))),
                DocCons(
                  DocText(","),
                  DocText("null"))),
              DocCons(
                DocText(","),
                DocText("2"))),
            DocText("]")))
        render(seq) must_== expected
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = DocCons(
          DocText("{"),
          DocCons(
            DocNest(2, DocCons(
              DocBreak,
              DocCons(
                DocCons(
                  DocCons(
                    DocCons(
                      DocText("\"a\":"),
                      DocText("1")),
                    DocCons(
                      DocCons(DocText(","),
                        DocBreak),
                      DocCons(
                        DocText("\"b\":"),
                        DocText("null")))),
                  DocCons(
                    DocCons(
                      DocText(","),
                      DocBreak),
                    DocCons(
                      DocText("\"c\":"),
                      DocText("null")))),
                DocCons(
                  DocCons(
                    DocText(","),
                    DocBreak),
                  DocCons(
                    DocText("\"d\":"),
                    DocText("2")))))),
            DocCons(
              DocBreak,
              DocText("}"))))
        render(map) must_== expected
      }
    }

  }

}
