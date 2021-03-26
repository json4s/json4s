package org.json4s

import org.scalatest.wordspec.AnyWordSpec

import com.fasterxml.jackson.databind.ObjectMapper
import org.json4s.jackson.Json4sScalaModule

// fix https://github.com/json4s/json4s/issues/603
class JacksonDeserializationSpec extends AnyWordSpec {
  val mapper = new ObjectMapper
  mapper.registerModule(Json4sScalaModule)

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
