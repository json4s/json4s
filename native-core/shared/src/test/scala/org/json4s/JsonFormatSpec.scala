package org.json4s

import org.json4s.native.Document
import org.scalatest.wordspec.AnyWordSpec

class NativeJsonFormatSpec extends JsonFormatSpec[Document]("native") with native.JsonMethods

abstract class JsonFormatSpec[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  import DefaultReaders._

  def read[A](value: JValue)(implicit reader: Reader[A]): A = reader.read(value)

  s"$mod JsonFormat" should {
    "read a JLong" in {
      val value: JValue = JLong(42L)

      assert(read[Byte](value) == (42: Byte))
      assert(read[Short](value) == (42: Short))
      assert(read[Int](value) == 42)
      assert(read[Long](value) == 42L)
    }
  }
}
