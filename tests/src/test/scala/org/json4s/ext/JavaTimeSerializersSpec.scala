package org.json4s.ext

import java.time._
import java.time.temporal.ChronoUnit

import org.json4s._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

/**
 * System under specification for JavaTimeSerializer.
 */
abstract class JavaDateTimeSerializerSpec(mod: String) extends AnyWordSpec with ScalaCheckDrivenPropertyChecks {

  def s: Serialization
  implicit lazy val formats: Formats = s.formats(NoTypeHints) ++ JavaTimeSerializers.all

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10000)

  private implicit val zoneIdArbitrary: Arbitrary[ZoneId] =
    Arbitrary(
      Gen.oneOf(
        ZoneId.getAvailableZoneIds.toArray(Array.empty[String]).map(ZoneId.of).toSeq
      )
    )
  private implicit val instantArbitrary: Arbitrary[Instant] = Arbitrary(
    implicitly[Arbitrary[Int]].arbitrary.map(x => Instant.ofEpochMilli(x))
  )

  private implicit val localDateTimeArbitrary: Arbitrary[LocalDateTime] = Arbitrary(
    for {
      x1 <- implicitly[Arbitrary[Instant]].arbitrary
      x2 <- implicitly[Arbitrary[ZoneId]].arbitrary
    } yield LocalDateTime.ofInstant(x1, x2)
  )

  private implicit val durationArbitrary: Arbitrary[Duration] = Arbitrary(
    for {
      x1 <- implicitly[Arbitrary[Int]].arbitrary
      x2 <- Gen.oneOf(
        ChronoUnit.MILLIS,
        ChronoUnit.SECONDS,
        ChronoUnit.MINUTES,
        ChronoUnit.HOURS,
        ChronoUnit.DAYS,
      )
    } yield Duration.of(x1, x2)
  )

  private implicit val javaTimesArbitrary: Arbitrary[JavaTypes] =
    Arbitrary(Gen.resultOf(JavaTypes.apply(_, _, _, _, _, _, _, _, _)))

  (mod + " JavaTimeSerializer Specification") should {
    "scalacheck test" in forAll { (x: JavaTypes) =>
      val serialized = s.write(x)
      val actual = s.read[JavaTypes](serialized)
      assert(actual == x)
    }

    "Serialize java time types" in {
      val x = JavaTypes(
        Duration.ofDays(1),
        Instant.ofEpochMilli(1433890789),
        Year.of(1987),
        LocalDateTime.of(2015, 6, 23, 14, 34, 29),
        LocalDate.of(2015, 10, 17),
        LocalTime.of(15, 13, 34, 123),
        Period.of(2014, 11, 22),
        YearMonth.of(1934, 12),
        MonthDay.of(11, 24)
      )
      val ser = s.write(x)
      assert(s.read[JavaTypes](ser) == x)
    }

    "LocalDateTime use configured date format" in {
      implicit val formats: Formats = new DefaultFormats {
        override def dateFormatter = {
          val f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
          f.setTimeZone(DefaultFormats.UTC)
          f
        }
      } ++ JavaTimeSerializers.all

      val x = JavaDates(LocalDateTime.of(2015, 6, 24, 15, 34, 59))
      val ser = s.write(x)
      assert(ser == """{"ldt":"2015-06-24 15:34:59Z"}""")
    }

    "null is serialized as JSON null" in {
      val x = JavaTypes(null, null, null, null, null, null, null, null, null)
      val ser = s.write(x)
      assert(s.read[JavaTypes](ser) == x)
    }
  }
}

case class JavaTypes(
  duration: Duration,
  instant: Instant,
  year: Year,
  localDateTime: LocalDateTime,
  localDate: LocalDate,
  localTime: LocalTime,
  period: Period,
  yearMonth: YearMonth,
  monthDay: MonthDay
)

case class JavaDates(ldt: LocalDateTime)
