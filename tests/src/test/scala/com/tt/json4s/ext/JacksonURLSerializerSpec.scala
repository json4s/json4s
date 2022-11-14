package com.tt.json4s
package ext

class JacksonURLSerializerSpec extends URLSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}
