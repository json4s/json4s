package com.tt.json4s
package ext

class JacksonJodaTimeSerializerSpec extends JodaTimeSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
  val m: JsonMethods[_] = jackson.JsonMethods
}
