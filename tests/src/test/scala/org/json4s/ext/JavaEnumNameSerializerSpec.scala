package org.json4s
package ext

import org.json4s.jackson._
import org.specs2.mutable.Specification

private case class ExampleClass(day: JavaDayEnum)

class JavaEnumNameSerializerSpec extends Specification {

  implicit val formats: Formats =
    DefaultFormats + new JavaEnumNameSerializer[JavaDayEnum]()

  "An java enum serializer" should {
    "serialize and deserialize with name" in {
      val example = ExampleClass(JavaDayEnum.Monday)
      val jsonString = Serialization.write(example)
      jsonString must_== """{"day":"Monday"}"""
      Serialization.read[ExampleClass](jsonString) must_== example
    }
  }

}
