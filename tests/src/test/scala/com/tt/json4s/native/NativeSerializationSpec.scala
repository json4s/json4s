package com.tt.json4s
package native

class NativeSerializationSpec
  extends SerializationSpec(
    serialization = Serialization,
    baseFormats = Serialization.formats(NoTypeHints)
  )
