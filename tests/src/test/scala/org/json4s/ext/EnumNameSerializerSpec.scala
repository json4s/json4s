package org.json4s
package ext

import org.specs2.mutable.Specification

object EnumNameSerializerSpec extends native.JsonMethods {

  object Days extends Enumeration {
    type Day = Value
    val Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday = Value
  }

  object Weekend extends Enumeration {
    type Weekend = Value
    val Saturday, Sunday = Value
  }

  object Week extends Enumeration {
    type Week = Value
    val Monday, Tuesday, Wednesday, Thursday, Friday = Value
  }

  implicit val formats: Formats =
    DefaultFormats + new EnumNameSerializer(Days) + new EnumNameSerializer(Weekend) + new EnumNameSerializer(Week)
}
class EnumNameSerializerSpec extends Specification {

  import EnumNameSerializerSpec._
  val weekdays = JArray("Monday, Tuesday, Wednesday, Thursday, Friday".split(",").map(s => JString(s.trim)).toList)
  val weekend = JArray("Saturday, Sunday".split(",").map(s => JString(s.trim)).toList)
  val week = JArray("Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday".split(",").map(s => JString(s.trim)).toList)

  "An enum name serializer" should {
    "deserialize week days" in {
      weekdays.extract[List[Week.Week]] must_== List(Week.Monday, Week.Tuesday, Week.Wednesday, Week.Thursday, Week.Friday)
    }

    "deserialize weekend" in {
      weekend.extract[List[Weekend.Weekend]] must_== List(Weekend.Saturday, Weekend.Sunday)
    }

  }

}
