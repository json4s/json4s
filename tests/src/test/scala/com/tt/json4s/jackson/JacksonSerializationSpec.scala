package com.tt.json4s.jackson

class JacksonSerializationSpec
  extends SerializationSpec(
    serialization = com.tt.json4s.jackson.Serialization,
    baseFormats = com.tt.json4s.jackson.Serialization.formats(NoTypeHints)
  )
