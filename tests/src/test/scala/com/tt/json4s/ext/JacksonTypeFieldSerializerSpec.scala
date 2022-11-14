package com.tt.json4s
package ext

import com.tt.json4s.Serialization

class JacksonTypeFieldSerializerSpec extends TypeFieldSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}
