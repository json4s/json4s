package org.json4s
package jackson

class JacksonSerializationSpec
  extends SerializationSpec(
    serialization = org.json4s.jackson.Serialization,
    baseFormats = org.json4s.jackson.Serialization.formats(NoTypeHints)
  )
