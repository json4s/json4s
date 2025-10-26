package org.json4s

class JacksonJsonFormatsSpec extends JsonFormatsSpec[JValue]("Jackson") with jackson.JsonMethods {
  "Unicode escaping can be changed" should {
    val json = parse("{\"Script Small G\": \"\u210A\"}")

    "escaped" in {
      assert(compact(render(json), alwaysEscapeUnicode = true) == "{\"Script Small G\":\"\\u210A\"}")
    }

    "not escaped" in {
      assert(compact(render(json), alwaysEscapeUnicode = false) == "{\"Script Small G\":\"\u210A\"}")
    }
  }
}
