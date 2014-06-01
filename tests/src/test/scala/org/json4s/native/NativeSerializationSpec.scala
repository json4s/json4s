package org.json4s
package native

object NativeSerializationSpec
  extends SerializationSpec(
    serialization = Serialization,
    baseFormats = Serialization.formats(NoTypeHints)
  )
