package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.DefaultJsonFormats._

class ReaderSpec extends AnyWordSpec {
  "Reader" should {
    "accumulate errors" in {
      case class A(x: Int, y: String)
      val reader: Reader[A] =
        Reader.reader2((x: Int, y: String) => A(x, y))("x", "y")
      val Left(res: MappingException.Multi) = reader.readEither(JObject(List("a" -> JInt(1), "b" -> JInt(2))))
      assert(res.errors.map(_.getMessage) == Seq("field x not found", "field y not found"))
    }
  }
}
