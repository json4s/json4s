package org.json4s

import org.json4s.native.Document
import org.specs2.mutable.Specification

class NativeJsonFormatSpec extends JsonFormatSpec[Document]("native") with native.JsonMethods
class JacksonJsonFormatSpec extends JsonFormatSpec[JValue]("jackson") with jackson.JsonMethods

abstract class JsonFormatSpec[T](mod: String) extends Specification with JsonMethods[T] {

  import DefaultReaders._

  def read[A](value: JValue)(implicit reader: Reader[A]): A = reader.read(value)

  s"$mod JsonFormat" should {
    "read a JLong" in {
      val value: JValue = JLong(42L)

      read[Byte](value) must_=== (42: Byte)
      read[Short](value) must_=== (42: Short)
      read[Int](value) must_=== 42
      read[Long](value) must_=== 42L
    }
  }
}
