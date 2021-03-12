package org.json4s

import org.specs2.mutable.Specification

import com.fasterxml.jackson.databind.ObjectMapper
import org.json4s.jackson.Json4sScalaModule

// fix https://github.com/json4s/json4s/issues/603
class JacksonDeserializationSpec extends Specification {
  val mapper = new ObjectMapper
  mapper.registerModule(Json4sScalaModule)

  "Recursive deserialization" should {

    "create JObject by assigning to 'JValue'" in {
      mapper.readValue("""{"x":"y"}""", classOf[JValue]) must_== JObject(List(("x", JString("y"))))
    }

    "create JObject by assigning to 'JObject'" in {
      mapper.readValue("""{"x":"y"}""", classOf[JObject]) must_== JObject(List(("x", JString("y"))))
    }

    "create JArray by assigning to 'JValue'" in {
      mapper.readValue("""["x", "y"]""", classOf[JValue]) must_== JArray(List(JString("x"), JString("y")))
    }

    "create JArray by assigning to 'JArray'" in {
      mapper.readValue("""["x", "y"]""", classOf[JArray]) must_== JArray(List(JString("x"), JString("y")))
    }
  }
}
