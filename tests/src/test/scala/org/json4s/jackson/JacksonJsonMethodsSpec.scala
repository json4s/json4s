package org.json4s
package jackson

import org.scalatest.wordspec.AnyWordSpec

class JacksonJsonMethodsSpec extends AnyWordSpec {

  import org.json4s.JsonDSL._
  import JsonMethods._

  "JsonMethods.write" should {
    "produce JSON without empty fields" should {

      implicit val format = DefaultFormats.skippingEmptyValues

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

      implicit val format = DefaultFormats.preservingEmptyValues

      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = JArray(List(JInt(1), JNull, JNull, JInt(2)))
        assert(render(seq) == expected)
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = JObject(List(("a", JInt(1)), ("b", JNull), ("c", JNull), ("d", JInt(2))))
        assert(render(map) == expected)
      }
    }

  }

}
