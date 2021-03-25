package org.json4s
package ext

class JacksonURISerializerSpec extends URISerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}
