package org.json4s.ext

import java.time._

import org.json4s._
import org.specs2.mutable.Specification


class NativeJavaDateTimeSerializerSpec extends JavaDateTimeSerializerSpec("Native") {
  val s: Serialization = native.Serialization
}

class JacksonJavaDateTimeSerializerSpec extends JavaDateTimeSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}

/**
 * System under specification for JavaTimeSerializer.
 */
abstract class JavaDateTimeSerializerSpec(mod: String) extends Specification {

  def s: Serialization
  implicit lazy val formats = s.formats(NoTypeHints) ++ JavaTimeSerializers.all

  (mod + " JavaTimeSerializer Specification") should {
    "Serialize java time types" in {
      val x = JavaTypes(Duration.ofDays(1),
        Instant.ofEpochMilli(1433890789),
        Year.of(1987),
        LocalDateTime.of(2015, 6, 23, 14, 34, 29),
        LocalDate.of(2015, 10, 17),
        LocalTime.of(15, 13, 34, 123),
        Period.of(2014, 11, 22),
        YearMonth.of(1934, 12),
        MonthDay.of(11, 24))
      val ser = s.write(x)
      s.read[JavaTypes](ser) must_== x
    }

    "LocalDateTime use configured date format" in {
      implicit val formats = new DefaultFormats {
        override def dateFormatter = {
          val f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
          f.setTimeZone(DefaultFormats.UTC)
          f
        }
      } ++ JavaTimeSerializers.all

      val x = JavaDates(LocalDateTime.of(2015, 6, 24, 15, 34, 59))
      val ser = s.write(x)
      ser must_== """{"ldt":"2015-06-24 15:34:59Z"}"""
    }

    "null is serialized as JSON null" in {
      val x = JavaTypes(null, null, null, null, null, null, null, null, null)
      val ser = s.write(x)
      s.read[JavaTypes](ser) must_== x
    }
  }
}

case class JavaTypes(duration: Duration,
  instant: Instant,
  year: Year,
  localDateTime: LocalDateTime,
  localDate: LocalDate,
  localTime: LocalTime,
  period: Period,
  yearMonth: YearMonth,
  monthDay: MonthDay)

case class JavaDates(ldt: LocalDateTime)
