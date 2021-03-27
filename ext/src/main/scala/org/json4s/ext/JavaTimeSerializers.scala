package org.json4s.ext

import java.time._
import java.util.{Date, TimeZone}

import org.json4s._

object JavaTimeSerializers {

  def all: List[Serializer[_]] = List[Serializer[_]](
    JLocalDateTimeSerializer,
    JZonedDateTimeSerializer,
    JOffsetDateTimeSerializer,
    JDurationSerializer,
    JInstantSerializer,
    JYearSerializer,
    JLocalDateSerializer(),
    JLocalTimeSerializer(),
    JPeriodSerializer(),
    JYearMonthSerializer(),
    JMonthDaySerializer()
  )

  private[ext] def getZoneOffset(timezone: TimeZone): ZoneOffset = {
    OffsetDateTime.now(timezone.toZoneId).getOffset
  }
}

case object JLocalDateTimeSerializer
  extends CustomSerializer[LocalDateTime](format =>
    (
      {
        case JString(s) =>
          val zonedInstant = DateParser.parse(s, format)
          LocalDateTime.ofInstant(Instant.ofEpochMilli(zonedInstant.instant), zonedInstant.timezone.toZoneId)
        case JNull => null
      },
      { case d: LocalDateTime =>
        JString(
          format.dateFormat.format(
            Date.from(d.toInstant(JavaTimeSerializers.getZoneOffset(format.dateFormat.timezone)))
          )
        )
      }
    )
  )

case object JZonedDateTimeSerializer
  extends CustomSerializer[ZonedDateTime](format =>
    (
      {
        case JString(s) =>
          val zonedInstant = DateParser.parse(s, format)
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(zonedInstant.instant), zonedInstant.timezone.toZoneId)
        case JNull => null
      },
      { case d: ZonedDateTime =>
        JString(format.dateFormat.format(Date.from(d.toInstant())))
      }
    )
  )

case object JOffsetDateTimeSerializer
  extends CustomSerializer[OffsetDateTime](format =>
    (
      {
        case JString(s) =>
          val zonedInstant = DateParser.parse(s, format)
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(zonedInstant.instant), zonedInstant.timezone.toZoneId)
        case JNull => null
      },
      { case d: OffsetDateTime =>
        JString(format.dateFormat.format(Date.from(d.toInstant())))
      }
    )
  )

case object JDurationSerializer
  extends CustomSerializer[Duration](format =>
    (
      {
        case JInt(d) => Duration.ofMillis(d.toLong)
        case JNull => null
      },
      { case d: Duration =>
        JInt(d.toMillis)
      }
    )
  )

case object JInstantSerializer
  extends CustomSerializer[Instant](format =>
    (
      {
        case JInt(d) => Instant.ofEpochMilli(d.toLong)
        case JNull => null
      },
      { case d: Instant =>
        JInt(d.toEpochMilli)
      }
    )
  )

case object JYearSerializer
  extends CustomSerializer[Year](format =>
    (
      {
        case JInt(n) => Year.of(n.toInt)
        case JNull => null
      },
      { case y: Year =>
        JInt(y.getValue)
      }
    )
  )

private[ext] case class _JLocalDate(year: Int, month: Int, day: Int)
object JLocalDateSerializer {
  def apply(): Serializer[LocalDate] = new ClassSerializer(new ClassType[LocalDate, _JLocalDate]() {
    def unwrap(d: _JLocalDate)(implicit format: Formats) = LocalDate.of(d.year, d.month, d.day)
    def wrap(d: LocalDate)(implicit format: Formats) =
      _JLocalDate(d.getYear(), d.getMonthValue, d.getDayOfMonth)
  })
}

private[ext] case class _JLocalTime(hour: Int, minute: Int, second: Int, millis: Int)
object JLocalTimeSerializer {
  def apply(): Serializer[LocalTime] = new ClassSerializer(new ClassType[LocalTime, _JLocalTime]() {
    def unwrap(t: _JLocalTime)(implicit format: Formats) =
      LocalTime.of(t.hour, t.minute, t.second, t.millis)
    def wrap(t: LocalTime)(implicit format: Formats) =
      _JLocalTime(t.getHour, t.getMinute, t.getSecond, t.getNano)
  })
}

private[ext] case class _JPeriod(year: Int, month: Int, day: Int)
object JPeriodSerializer {
  def apply(): Serializer[Period] = new ClassSerializer(new ClassType[Period, _JPeriod]() {
    def unwrap(p: _JPeriod)(implicit format: Formats) = Period.of(p.year, p.month, p.day)
    def wrap(p: Period)(implicit format: Formats) = _JPeriod(p.getYears, p.getMonths, p.getDays)
  })
}

private[ext] case class _JYearMonth(year: Int, month: Int)
object JYearMonthSerializer {
  def apply(): Serializer[YearMonth] = new ClassSerializer(new ClassType[YearMonth, _JYearMonth]() {
    def unwrap(ym: _JYearMonth)(implicit format: Formats) = YearMonth.of(ym.year, ym.month)
    def wrap(ym: YearMonth)(implicit format: Formats) = _JYearMonth(ym.getYear, ym.getMonthValue)
  })
}

private[ext] case class _JMonthDay(month: Int, dayOfMonth: Int)
object JMonthDaySerializer {
  def apply(): Serializer[MonthDay] = new ClassSerializer(new ClassType[MonthDay, _JMonthDay]() {
    def unwrap(md: _JMonthDay)(implicit format: Formats) = MonthDay.of(md.month, md.dayOfMonth)
    def wrap(md: MonthDay)(implicit format: Formats) = _JMonthDay(md.getMonthValue, md.getDayOfMonth)
  })
}
