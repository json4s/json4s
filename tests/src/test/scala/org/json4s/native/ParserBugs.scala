package org.json4s


import util.control.Exception._
import org.specs2.mutable.Specification

class ParserBugs extends Specification {
  import native.JsonParser
  import native.JsonMethods._

  "For ParserBugs" should {
    "Unicode ffff is a valid char in string literal" in {
      parseOpt(""" {"x":"\uffff"} """).isDefined must_== true
    }

    "Does not hang when parsing 2.2250738585072012e-308" in {
      allCatch.opt(parse(""" [ 2.2250738585072012e-308 ] """)) must_== None
      allCatch.opt(parse(""" [ 22.250738585072012e-309 ] """)) must_== None
    }

    "Does not allow colon at start of array (1039)" in {
      parseOpt("""[:"foo", "bar"]""") must_== None
    }

    "Does not allow colon instead of comma in array (1039)" in {
      parseOpt("""["foo" : "bar"]""") must_== None
    }

    "Solo quote mark should fail cleanly (not StringIndexOutOfBoundsException) (1041)" in {
      JsonParser.parse("\"", discardParser) must throwA[Throwable].like {
        case e: ParserUtil.ParseException => e.getMessage must startWith("unexpected eof")
      }
    }

    "Field names must be quoted" in {
      val json = JObject(List(JField("foo\nbar", JInt(1))))
      val s = compact(render(json))
      s must_== """{"foo\nbar":1}"""
      parse(s) must_== json
    }
  }

  private[this] val discardParser = (p : JsonParser.Parser) => {
     var token: JsonParser.Token = null
     do {
       token = p.nextToken
     } while (token != JsonParser.End)
   }
}
