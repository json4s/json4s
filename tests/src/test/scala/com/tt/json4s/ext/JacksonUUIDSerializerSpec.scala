package com.tt.json4s
package ext

class JacksonUUIDSerializerSpec extends UUIDSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}
