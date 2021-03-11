package org.json4s
package native

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.prefs.EmptyValueStrategy

class NativeJsonMethodsSpec extends AnyWordSpec {

  import org.json4s.JsonDSL._
  import JsonMethods._

  "JsonMethods.parse" should {

    val stringJson = """{"number": 200}"""

    "parse StringInput and produce JInt" in {
      assert((parse(stringJson) \ "number").isInstanceOf[JInt])
    }

    "parse ReaderInput and produce JInt" in {
      assert((parse(new java.io.StringReader(stringJson)) \ "number").isInstanceOf[JInt])
    }

    "parse StreamInput and produce JInt" in {
      assert((parse(new java.io.ByteArrayInputStream(stringJson.getBytes)) \ "number").isInstanceOf[JInt])
    }

    "parse StringInput and produce JLong" in {
      assert((parse(stringJson, useBigIntForLong = false) \ "number").isInstanceOf[JLong])
    }

    "parse ReaderInput and produce JLong" in {
      assert((parse(new java.io.StringReader(stringJson), useBigIntForLong = false) \ "number").isInstanceOf[JLong])
    }

    "parse StreamInput and produce AST using Long" in {
      assert(
        (parse(
          new java.io.ByteArrayInputStream(stringJson.getBytes),
          useBigIntForLong = false
        ) \ "number").isInstanceOf[JLong]
      )
    }
  }

  "JsonMethods.write" should {

    "produce JSON without empty fields" should {
      implicit val formats: Formats = DefaultFormats.skippingEmptyValues

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected =
          DocCons(DocText("["), DocCons(DocCons(DocText("1"), DocCons(DocText(","), DocText("2"))), DocText("]")))
        assert(render(seq) == expected)
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = DocCons(
          DocText("{"),
          DocCons(
            DocNest(
              2,
              DocCons(
                DocBreak,
                DocCons(
                  DocCons(DocText("\"a\":"), DocText("1")),
                  DocCons(DocCons(DocText(","), DocBreak), DocCons(DocText("\"d\":"), DocText("2")))
                )
              )
            ),
            DocCons(DocBreak, DocText("}"))
          )
        )
        assert(render(map) == expected)
      }
    }

    "produce JSON with empty fields preserved" should {

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = DocCons(
          DocText("["),
          DocCons(
            DocCons(
              DocCons(
                DocCons(DocText("1"), DocCons(DocText(","), DocText("null"))),
                DocCons(DocText(","), DocText("null"))
              ),
              DocCons(DocText(","), DocText("2"))
            ),
            DocText("]")
          )
        )
        assert(render(seq, emptyValueStrategy = EmptyValueStrategy.preserve) == expected)
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = DocCons(
          DocText("{"),
          DocCons(
            DocNest(
              2,
              DocCons(
                DocBreak,
                DocCons(
                  DocCons(
                    DocCons(
                      DocCons(DocText("\"a\":"), DocText("1")),
                      DocCons(DocCons(DocText(","), DocBreak), DocCons(DocText("\"b\":"), DocText("null")))
                    ),
                    DocCons(DocCons(DocText(","), DocBreak), DocCons(DocText("\"c\":"), DocText("null")))
                  ),
                  DocCons(DocCons(DocText(","), DocBreak), DocCons(DocText("\"d\":"), DocText("2")))
                )
              )
            ),
            DocCons(DocBreak, DocText("}"))
          )
        )
        assert(render(map, emptyValueStrategy = EmptyValueStrategy.preserve) == expected)
      }
    }

  }

}
