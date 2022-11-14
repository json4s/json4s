package com.tt.json4s.jackson

import org.scalatest.wordspec.AnyWordSpec
import com.tt.json4s.JObject
import com.tt.json4s.JInt

class JacksonJsonMethodsSpec extends AnyWordSpec {
  "jackson.JsonMethods" should {
    "parse string" in {
      // https://github.com/json4s/json4s/issues/814
      val x = JsonMethods.parse(""" {"x" : 2 } """)
      assert(x == JObject("x" -> JInt(2)))
    }
  }
}
