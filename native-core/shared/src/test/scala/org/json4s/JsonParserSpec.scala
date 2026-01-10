package org.json4s

import org.json4s.ParserUtil.ParseException
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.Checkers

/**
 * System under specification for JSON Parser.
 */
class JsonParserSpec extends AnyWordSpec with JValueGen with Checkers {
  import native.JsonMethods.*
  import native.JsonParser
  import native.Printer

  "A JSON Parser" should {
    "avoid ClassCastException" in {
      // https://github.com/json4s/json4s/pull/1394
      Seq("{[]]", "{ \"foo\" : }").foreach { str =>
        val e = intercept[ParseException](parse(str))
        assert(e.getCause == null, str)
      }
    }

    "Any valid json can be parsed" in check { (json: JValue) =>
      parse(Printer.pretty(render(json)))
      true
    }

    "Empty Json is parsed to JObject" in {
      assert(parse("{}") == JObject())
    }

    "Buffer size does not change parsing result" in {
      val bufSize = Gen.choose(2, 64)
      forAll(genObject, bufSize, bufSize) { (x: JValue, s1: Int, s2: Int) =>
        parseVal(x, s1) == parseVal(x, s2)
      }
    }

    "All valid string escape characters can be parsed" in {
      assert(parse("[\"abc\\\"\\\\\\/\\b\\f\\n\\r\\t\\u00a0\"]") == JArray(JString("abc\"\\/\b\f\n\r\t\u00a0") :: Nil))
    }

    "Unclosed string literal fails parsing" in {
      assert(parseOpt("{\"foo\":\"sd") == None)
      assert(parseOpt("{\"foo\":\"sd}") == None)
    }

    "parses doubles as bigdecimal" in {
      assert(JsonParser.parse("[1.234]", useBigDecimalForDouble = true) == JArray(JDecimal(BigDecimal("1.234")) :: Nil))
    }

    "parse -1.40737488355328E+15 as bigdecimal" in {
      val bd = BigDecimal("-1.40737488355328E+15")
      assert(JsonParser.parse("[-1.40737488355328E+15]", useBigDecimalForDouble = true) == JArray(JDecimal(bd) :: Nil))
    }

    "parse -1.40737488355328E15 as bigdecimal" in {
      val bd = BigDecimal("-1.40737488355328E15")
      assert(JsonParser.parse("[-1.40737488355328E15]", useBigDecimalForDouble = true) == JArray(JDecimal(bd) :: Nil))
    }

    "parse -1.40737488355328E-15 as bigdecimal" in {
      val bd = BigDecimal("-1.40737488355328E-15")
      assert(JsonParser.parse("[-1.40737488355328E-15]", useBigDecimalForDouble = true) == JArray(JDecimal(bd) :: Nil))
    }

    "parse 9223372036854775808 as bigint" in {
      val bi = BigInt("9223372036854775808")
      assert(
        JsonParser.parse("[9223372036854775808]", useBigDecimalForDouble = true, useBigIntForLong = true) == JArray(
          JInt(bi) :: Nil
        )
      )
    }

    "parse -9223372036854775809 as bigint" in {
      val bi = BigInt("-9223372036854775809")
      assert(
        JsonParser.parse("[-9223372036854775809]", useBigDecimalForDouble = true, useBigIntForLong = true) == JArray(
          JInt(bi) :: Nil
        )
      )
    }

    "parse 9223372036854775807 as long" in {
      val l = Long.MaxValue
      assert(
        JsonParser.parse("[9223372036854775807]", useBigDecimalForDouble = true, useBigIntForLong = false) == JArray(
          JLong(l) :: Nil
        )
      )
    }

    "parse -9223372036854775808 as long" in {
      val l = Long.MinValue
      assert(
        JsonParser.parse(
          "[-9223372036854775808]",
          useBigDecimalForDouble = true,
          useBigIntForLong = false
        ) == JArray(JLong(l) :: Nil)
      )
    }

    "parse true as boolean" in {
      assert(JsonParser.parse("true") == JBool(true))
    }

    "The EOF has reached when the Reader returns EOF" in {
      class StingyReader(s: String) extends java.io.StringReader(s) {
        override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
          val c = read()
          if (c == -1) -1
          else {
            cbuf(off) = c.toChar
            1
          }
        }
      }

      val json = JsonParser.parse(new StingyReader(""" ["hello"] """))
      assert(json == JArray(JString("hello") :: Nil))
    }
  }

  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genObject)

  private def parseVal(json: JValue, bufSize: Int) = {
    val existingSize = Segments.segmentSize
    try {
      Segments.segmentSize = bufSize
      Segments.clear()
      JsonParser.parse(compact(render(json)))
    } finally {
      Segments.segmentSize = existingSize
    }
  }
}
