package org.json4s
package ext

import org.json4s.jackson._
import org.scalatest.wordspec.AnyWordSpec

private case class ExampleClass(day: JavaDayEnum)

class JavaEnumNameSerializerSpec extends AnyWordSpec {

  implicit val formats: Formats =
    DefaultFormats + new JavaEnumNameSerializer[JavaDayEnum]()

  "An java enum serializer" should {
    "serialize and deserialize with name" in {
      val example = ExampleClass(JavaDayEnum.Monday)
      val jsonString = Serialization.write(example)
      assert(jsonString == """{"day":"Monday"}""")
      assert(Serialization.read[ExampleClass](jsonString) == example)
    }
  }

}
