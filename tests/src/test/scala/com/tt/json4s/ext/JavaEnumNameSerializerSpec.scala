package com.tt.json4s
package ext

import org.scalatest.wordspec.AnyWordSpec

private case class ExampleClass(day: JavaDayEnum)

abstract class JavaEnumNameSerializerSpec(
  serialization: Serialization
) extends AnyWordSpec {

  implicit val formats: Formats =
    DefaultFormats + new JavaEnumNameSerializer[JavaDayEnum]()

  "An java enum serializer" should {
    "serialize and deserialize with name" in {
      val example = ExampleClass(JavaDayEnum.Monday)
      val jsonString = serialization.write(example)
      assert(jsonString == """{"day":"Monday"}""")
      assert(serialization.read[ExampleClass](jsonString) == example)
    }
  }

}
