package org.json4s

import util.control.Exception._
import org.scalatest.wordspec.AnyWordSpec

class ParserBugs extends AnyWordSpec {
  import native.JsonParser
  import native.JsonMethods._

  "For ParserBugs" should {
    "Unicode ffff is a valid char in string literal" in {
      assert(parseOpt(""" {"x":"\uffff"} """).isDefined)
    }

    "Does not hang when parsing 2.2250738585072012e-308" in {
      assert(allCatch.opt(parse(""" [ 2.2250738585072012e-308 ] """)).isEmpty)
      assert(allCatch.opt(parse(""" [ 22.250738585072012e-309 ] """)).isEmpty)
    }

    "Does not allow colon at start of array (1039)" in {
      assert(parseOpt("""[:"foo", "bar"]""").isEmpty)
    }

    "Does not allow colon instead of comma in array (1039)" in {
      assert(parseOpt("""["foo" : "bar"]""").isEmpty)
    }

    "Solo quote mark should fail cleanly (not StringIndexOutOfBoundsException) (1041)" in {
      try {
        JsonParser.parse("\"", discardParser)
        fail("should be throw ParseException")
      } catch {
        case e: ParserUtil.ParseException =>
          assert(e.getMessage.startsWith("unexpected eof"))
      }
    }

    "Field names must be quoted" in {
      val json = JObject(List(JField("foo\nbar", JInt(1))))
      val s = compact(render(json))
      assert(s == """{"foo\nbar":1}""")
      assert(parse(s) == json)
    }
  }

  private[this] val discardParser = (p: JsonParser.Parser) => {
    var token: JsonParser.Token = p.nextToken
    while (token != JsonParser.End) {
      token = p.nextToken
    }
  }
}
