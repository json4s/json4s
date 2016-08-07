package org.json4s

import org.specs2.mutable.Specification
import java.io.StringWriter

class JsonWriterSpec extends Specification {
  "JsonWriter" should {
    "https://github.com/json4s/json4s/issues/393" in {
      val writer = JsonWriter.streaming(new StringWriter)
      writer.addJValue(JLong(42))
      writer.result.toString must_== "42"
    }
  }
}
