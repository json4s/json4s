package com.tt.json4s.ext

import com.tt.json4s.Serialization

class NativeJavaDateTimeSerializerSpec extends JavaDateTimeSerializerSpec("Native") {
  val s: Serialization = com.tt.json4s.native.Serialization
}
