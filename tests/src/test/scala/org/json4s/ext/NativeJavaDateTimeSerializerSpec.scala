package org.json4s.ext

import org.json4s.Serialization

class NativeJavaDateTimeSerializerSpec extends JavaDateTimeSerializerSpec("Native") {
  val s: Serialization = org.json4s.native.Serialization
}
