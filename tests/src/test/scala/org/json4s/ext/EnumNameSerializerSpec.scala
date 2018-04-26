package org.json4s
package ext

import org.specs2.mutable.Specification

object EnumNameSerializerSpec extends native.JsonMethods {

  object Days extends Enumeration {
    type Day = Value
    val Monday = Value(1)
    val Tuesday = Value(2)
    val Wednesday = Value(3)
    val Thursday = Value(4)
    val Friday = Value(5)
    val Saturday = Value(6)
    val Sunday = Value(7)
  }

  object Weekend extends Enumeration {
    type Weekend = Value
    val Saturday = Value(6)
    val Sunday = Value(7)
  }

  object Week extends Enumeration {
    type Week = Value
    val Monday = Value(1)
    val Tuesday = Value(2)
    val Wednesday = Value(3)
    val Thursday = Value(4)
    val Friday = Value(5)
  }

}

class EnumNameSerializerSpec extends Specification {
  import EnumNameSerializerSpec._

  val weekdays = List(Week.Monday, Week.Tuesday, Week.Wednesday, Week.Thursday, Week.Friday)
  val weekend = List(Weekend.Saturday, Weekend.Sunday)

  val weekdayNames = JArray(weekdays.map(s => JString(s.toString)))
  val weekendNames = JArray(weekend.map(s => JString(s.toString)))

  val weekdayIds = JArray(weekdays.map(s => JInt(s.id)))
  val weekendIds = JArray(weekend.map(s => JInt(s.id)))

  "An enum serializer" should {

    "serialize with name" in {
      implicit val formats: Formats =
        DefaultFormats + new EnumNameSerializer(Days) + new EnumNameSerializer(Weekend) + new EnumNameSerializer(Week)
      "week days" in {
        Extraction.decompose(weekdays) must_== weekdayNames
      }

      "weekend" in {
        Extraction.decompose(weekend) must_== weekendNames
      }
    }


    "deserialize from name" in {
      implicit val formats: Formats =
        DefaultFormats + new EnumNameSerializer(Days) + new EnumNameSerializer(Weekend) + new EnumNameSerializer(Week)
      "week days" in {
        weekdayNames.extract[List[Week.Week]] must_== weekdays
      }

      "weekend" in {
        weekendNames.extract[List[Weekend.Weekend]] must_== weekend
      }
    }

    "serialize with id" in {
      implicit val formats: Formats =
        DefaultFormats + new EnumSerializer(Days) + new EnumSerializer(Weekend) + new EnumSerializer(Week)
      "week days" in {
        Extraction.decompose(weekdays) must_== weekdayIds
      }

      "weekend" in {
        Extraction.decompose(weekend) must_== weekendIds
      }
    }

    "deserialize from id" in {
      implicit val formats: Formats =
        DefaultFormats + new EnumSerializer(Days) + new EnumSerializer(Weekend) + new EnumSerializer(Week)
      "week days" in {
        weekdayIds.extract[List[Week.Week]] must_== weekdays
      }

      "weekend" in {
        weekendIds.extract[List[Weekend.Weekend]] must_== weekend
      }
    }

    "serialize with mix of id and name" in {
      implicit val formats: Formats =
        DefaultFormats  + new EnumNameSerializer(Week) + new EnumSerializer(Weekend)

      "week days as names" in {
        Extraction.decompose(weekdays) must_== weekdayNames
      }

      "weekends as ids" in {
        Extraction.decompose(weekend) must_== weekendIds
      }
    }

    "serialize with mix of id and name - inverted order of serializers in formats" in {
      implicit val formats: Formats =
        DefaultFormats  + new EnumSerializer(Weekend) + new EnumNameSerializer(Week)

      "week days as names" in {
        Extraction.decompose(weekdays) must_== weekdayNames
      }

      "weekends as ids" in {
        Extraction.decompose(weekend) must_== weekendIds
      }
    }

    "deserialize with mix of id and name" in {
      implicit val formats: Formats =
        DefaultFormats  + new EnumNameSerializer(Week) + new EnumSerializer(Weekend)

      "week days as names" in {
        weekdayNames.extract[List[Week.Week]] == weekdays
      }

      "weekends as ids" in {
        weekendIds.extract[List[Weekend.Weekend]] == weekend
      }
    }

    "deserialize with mix of id and name - inverted order of serializers in formats" in {
      implicit val formats: Formats =
        DefaultFormats  + new EnumSerializer(Weekend) + new EnumNameSerializer(Week)

      "week days as names" in {
        weekdayNames.extract[List[Week.Week]] == weekdays
      }

      "weekends as ids" in {
        weekendIds.extract[List[Weekend.Weekend]] == weekend
      }
    }

  }

}
