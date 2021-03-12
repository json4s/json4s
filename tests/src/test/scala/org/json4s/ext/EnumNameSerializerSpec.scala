package org.json4s
package ext

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

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

class EnumNameSerializerSpec extends AnyWordSpec {
  import EnumNameSerializerSpec._

  val weekdays = List(Week.Monday, Week.Tuesday, Week.Wednesday, Week.Thursday, Week.Friday)
  val weekend = List(Weekend.Saturday, Weekend.Sunday)

  val weekdayNames = JArray(weekdays.map(s => JString(s.toString)))
  val weekendNames = JArray(weekend.map(s => JString(s.toString)))

  val weekdayIds = JArray(weekdays.map(s => JInt(s.id)))
  val weekendIds = JArray(weekend.map(s => JInt(s.id)))

  "An enum serializer" should {

    "serialize with name" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumNameSerializer(Days) + new EnumNameSerializer(Weekend) + new EnumNameSerializer(Week)
      "week days" in {
        assert(Extraction.decompose(weekdays) == weekdayNames)
      }

      "weekend" in {
        assert(Extraction.decompose(weekend) == weekendNames)
      }
    }

    "deserialize from name" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumNameSerializer(Days) + new EnumNameSerializer(Weekend) + new EnumNameSerializer(Week)
      "week days" in {
        assert(weekdayNames.extract[List[Week.Week]] == weekdays)
      }

      "weekend" in {
        assert(weekendNames.extract[List[Weekend.Weekend]] == weekend)
      }
    }

    "serialize with id" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumSerializer(Days) + new EnumSerializer(Weekend) + new EnumSerializer(Week)
      "week days" in {
        assert(Extraction.decompose(weekdays) == weekdayIds)
      }

      "weekend" in {
        assert(Extraction.decompose(weekend) == weekendIds)
      }
    }

    "deserialize from id" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumSerializer(Days) + new EnumSerializer(Weekend) + new EnumSerializer(Week)
      "week days" in {
        assert(weekdayIds.extract[List[Week.Week]] == weekdays)
      }

      "weekend" in {
        assert(weekendIds.extract[List[Weekend.Weekend]] == weekend)
      }
    }

    "serialize with mix of id and name" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumNameSerializer(Week) + new EnumSerializer(Weekend)

      "week days as names" in {
        assert(Extraction.decompose(weekdays) == weekdayNames)
      }

      "weekends as ids" in {
        assert(Extraction.decompose(weekend) == weekendIds)
      }
    }

    "serialize with mix of id and name - inverted order of serializers in formats" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumSerializer(Weekend) + new EnumNameSerializer(Week)

      "week days as names" in {
        assert(Extraction.decompose(weekdays) == weekdayNames)
      }

      "weekends as ids" in {
        assert(Extraction.decompose(weekend) == weekendIds)
      }
    }

    "deserialize with mix of id and name" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumNameSerializer(Week) + new EnumSerializer(Weekend)

      "week days as names" in {
        assert(weekdayNames.extract[List[Week.Week]] == weekdays)
      }

      "weekends as ids" in {
        assert(weekendIds.extract[List[Weekend.Weekend]] == weekend)
      }
    }

    "deserialize with mix of id and name - inverted order of serializers in formats" should {
      implicit val formats: Formats =
        DefaultFormats + new EnumSerializer(Weekend) + new EnumNameSerializer(Week)

      "week days as names" in {
        assert(weekdayNames.extract[List[Week.Week]] == weekdays)
      }

      "weekends as ids" in {
        assert(weekendIds.extract[List[Weekend.Weekend]] == weekend)
      }
    }

  }

}
