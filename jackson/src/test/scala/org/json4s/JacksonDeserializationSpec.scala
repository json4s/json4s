package org.json4s

import org.json4s.jackson.Json4sScalaModule
import org.scalatest.wordspec.AnyWordSpec
import tools.jackson.databind.json.JsonMapper

// fix https://github.com/json4s/json4s/issues/603
class JacksonDeserializationSpec extends AnyWordSpec {
  private val mapper =
    JsonMapper
      .builder()
      .addModule(
        new Json4sScalaModule
      )
      .build()

  "Recursive deserialization" should {

    "create JObject by assigning to 'JValue'" in {
      assert(mapper.readValue("""{"x":"y"}""", classOf[JValue]) == JObject(List(("x", JString("y")))))
    }

    "create JObject by assigning to 'JObject'" in {
      assert(mapper.readValue("""{"x":"y"}""", classOf[JObject]) == JObject(List(("x", JString("y")))))
    }

    "create JArray by assigning to 'JValue'" in {
      assert(mapper.readValue("""["x", "y"]""", classOf[JValue]) == JArray(List(JString("x"), JString("y"))))
    }

    "create JArray by assigning to 'JArray'" in {
      assert(mapper.readValue("""["x", "y"]""", classOf[JArray]) == JArray(List(JString("x"), JString("y"))))
    }
  }
}
