package org.json4s


import org.specs.{ScalaCheck, Specification}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop._


/**
* System under specification for JSON Parser.
*/
object JsonParserSpec extends Specification("JSON Parser Specification") with JValueGen with ScalaCheck {
  import scala.text.Document
  import native.{JsonParser,Printer}
  import native.JsonMethods._

  "Any valid json can be parsed" in {
    val parsing = (json: JValue) => { parse(Printer.pretty(render(json))); true }
    forAll(parsing) must pass
  }

  "Buffer size does not change parsing result" in {
    val bufSize = Gen.choose(2, 64)
    val parsing = (x: JValue, s1: Int, s2: Int) => { parseVal(x, s1) == parseVal(x, s2) }
    forAll(genObject, bufSize, bufSize)(parsing) must pass
  }

  "Parsing is thread safe" in {
    import java.util.concurrent._

    val json = Examples.person
    val executor = Executors.newFixedThreadPool(100)
    val results = (0 to 100).map(_ =>
      executor.submit(new Callable[JValue] { def call = parse(json) })).toList.map(_.get)
    results.zip(results.tail).forall(pair => pair._1 == pair._2) mustEqual true
  }

  "All valid string escape characters can be parsed" in {
    parse("[\"abc\\\"\\\\\\/\\b\\f\\n\\r\\t\\u00a0\"]") must_== JArray(JString("abc\"\\/\b\f\n\r\t\u00a0")::Nil)
  }

  "Unclosed string literal fails parsing" in {
    parseOpt("{\"foo\":\"sd") mustEqual None
    parseOpt("{\"foo\":\"sd}") mustEqual None
  }

  "parses doubles as bigdecimal" in {
    JsonParser.parse("[1.234]", useBigDecimalForDouble = true) must_== JArray(JDecimal(BigDecimal("1.234"))::Nil)
  }

  "parse -1.40737488355328E+15 as bigdecimal" in {
    val bd = BigDecimal("-1.40737488355328E+15")
    JsonParser.parse("[-1.40737488355328E+15]", useBigDecimalForDouble = true) must_== JArray(JDecimal(bd) :: Nil)
  }

  "parse -1.40737488355328E15 as bigdecimal" in {
    val bd = BigDecimal("-1.40737488355328E15")
    JsonParser.parse("[-1.40737488355328E15]", useBigDecimalForDouble = true) must_== JArray(JDecimal(bd) :: Nil)
  }


  "parse -1.40737488355328E-15 as bigdecimal" in {
    val bd = BigDecimal("-1.40737488355328E-15")
    JsonParser.parse("[-1.40737488355328E-15]", useBigDecimalForDouble = true) must_== JArray(JDecimal(bd) :: Nil)
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
    json mustEqual JArray(JString("hello") :: Nil)
  }

  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genObject)

  private def parseVal(json: JValue, bufSize: Int) = {
    val existingSize = ParserUtil.Segments.segmentSize
    try {
      ParserUtil.Segments.segmentSize = bufSize
      ParserUtil.Segments.clear
      JsonParser.parse(compact(render(json)))
    } finally {
      ParserUtil.Segments.segmentSize = existingSize
    }
  }
}
