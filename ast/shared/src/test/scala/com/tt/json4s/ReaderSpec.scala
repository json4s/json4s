package com.tt.json4s

import org.scalatest.wordspec.AnyWordSpec
import com.tt.json4s.DefaultJsonFormats._

class ReaderSpec extends AnyWordSpec {
  "Reader" should {
    "accumulate errors" in {
      case class A(x: Int, y: String)
      val reader: Reader[A] =
        Reader.reader2((x: Int, y: String) => A(x, y))("x", "y")
      val Left(res1: MappingException.Multi) = reader.readEither(JObject(List("a" -> JInt(1), "b" -> JInt(2))))
      assert(res1.errors.map(_.getMessage) == Seq("field x not found", "field y not found"))
      val Left(res2: MappingException.Multi) =
        reader.readEither(JObject(List("x" -> JString("aaa"), "y" -> JObject(Nil))))
      assert(
        res2.errors.map(_.getMessage) == Seq(
          "Can't convert JString(aaa) to Int.",
          "Can't convert JObject(List()) to String."
        )
      )
    }
  }
}
