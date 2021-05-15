package org.json4s

import org.json4s.native.Document
import org.scalatest.wordspec.AnyWordSpec

class NativeJsonFormatSpec extends JsonFormatSpec[Document]("native") with native.JsonMethods

abstract class JsonFormatSpec[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  import DefaultReaders._

  implicit def jvalue2readerSyntax(j: JValue): ReaderSyntax = new ReaderSyntax(j)

  s"$mod JsonFormat" should {
    "read a JLong" in {
      val value: JValue = JLong(42L)

      assert(value.as[Byte] == (42: Byte))
      assert(value.as[Short] == (42: Short))
      assert(value.as[Int] == 42)
      assert(value.as[Long] == 42L)
    }
  }
}
