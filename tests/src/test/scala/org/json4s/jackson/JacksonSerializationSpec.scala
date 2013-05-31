package org.json4s
package jackson

object JacksonSerializationSpec extends SerializationSpec {

  val serialization = org.json4s.jackson.Serialization

  val baseFormats = org.json4s.jackson.Serialization.formats(NoTypeHints)

}