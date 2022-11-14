package com.tt.json4s.ext

import com.tt.json4s.Serialization

class JacksonJavaDateTimeSerializerSpec extends JavaDateTimeSerializerSpec("Jackson") {
  val s: Serialization = com.tt.json4s.jackson.Serialization
}
