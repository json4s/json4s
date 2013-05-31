package org.json4s
package native

object NativeSerializationSpec extends SerializationSpec {

  val serialization = Serialization

  val baseFormats = Serialization.formats(NoTypeHints)

}