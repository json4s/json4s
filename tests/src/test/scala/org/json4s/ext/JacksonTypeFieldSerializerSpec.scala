package org.json4s
package ext

class JacksonTypeFieldSerializerSpec extends TypeFieldSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}
