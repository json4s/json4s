package org.json4s
package jackson

import org.json4s.prefs.EmptyValueStrategy
import org.scalatest.wordspec.AnyWordSpec

class JacksonJsonMethodsSpec extends AnyWordSpec {

  import JsonMethods.*
  import org.json4s.JsonDSL.*

  "A JValue can be converted to a JsonNode." in {
    val jv = parse(""" { "numbers" : [1, 2], "foo": "bar" } """)
    println(asJsonNode(jv))
    parse(asJsonNode(jv).toString) == jv
  }

  "A JsonNode can be converted to a JValue." in {
    val jv = parse(""" { "numbers" : [1, 2], "foo": "bar" } """)
    fromJsonNode(asJsonNode(jv)) == jv
  }

  "JsonMethods.write" should {
    "produce JSON without empty fields" should {

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = JArray(List(JInt(1), JNothing, JNothing, JInt(2)))
        assert(render(seq) == expected)
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = JObject(List(("a", JInt(1)), ("b", JNothing), ("c", JNothing), ("d", JInt(2))))
        assert(render(map) == expected)
      }
    }

    "produce JSON with empty fields preserved" should {

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = JArray(List(JInt(1), JNull, JNull, JInt(2)))
        assert(render(seq, emptyValueStrategy = EmptyValueStrategy.preserve) == expected)
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = JObject(List(("a", JInt(1)), ("b", JNull), ("c", JNull), ("d", JInt(2))))
        assert(render(map, emptyValueStrategy = EmptyValueStrategy.preserve) == expected)
      }
    }

  }

}
