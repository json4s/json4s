package org.json4s

import java.io.StringWriter
import org.scalatest.wordspec.AnyWordSpec

class JsonWriterSpec extends AnyWordSpec {
  "JsonWriter" should {
    "https://github.com/json4s/json4s/issues/393" in {
      val writer = JsonWriter.streaming(new StringWriter, alwaysEscapeUnicode = false)
      writer.addJValue(JLong(42))
      assert(writer.result.toString == "42")
    }
  }
}
