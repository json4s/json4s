package org.json4s.ext

import org.json4s.Serialization

class JacksonJavaDateTimeSerializerSpec extends JavaDateTimeSerializerSpec("Jackson") {
  val s: Serialization = org.json4s.jackson.Serialization
}
