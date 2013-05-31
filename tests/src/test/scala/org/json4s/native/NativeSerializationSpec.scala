package org.json4s
package native

object NativeSerializationSpec extends SerializationSpec {

  val serialization = Serialization

  implicit val formats = Serialization.formats(NoTypeHints)

}