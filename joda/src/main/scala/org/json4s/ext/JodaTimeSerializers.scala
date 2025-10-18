/*
 * Copyright 2006-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s
package ext

import org.joda.time._

object JodaTimeSerializers {
  def all = List(
    DurationSerializer,
    InstantSerializer,
    DateTimeSerializer,
    DateMidnightSerializer,
    IntervalSerializer(),
    LocalDateSerializer(),
    LocalTimeSerializer(),
    PeriodSerializer
  )
}

case object PeriodSerializer
  extends CustomSerializer[Period](format =>
    (
      {
        case JString(p) => new Period(p)
        case JNull => null
      },
      { case p: Period =>
        JString(p.toString)
      }
    )
  )

case object DurationSerializer
  extends CustomSerializer[Duration](format =>
    (
      {
        case JInt(d) => new Duration(d.longValue)
        case JNull => null
      },
      { case d: Duration =>
        JInt(d.getMillis)
      }
    )
  )

case object InstantSerializer
  extends CustomSerializer[Instant](format =>
    (
      {
        case JInt(i) => new Instant(i.longValue)
        case JNull => null
      },
      { case i: Instant =>
        JInt(i.getMillis)
      }
    )
  )

case object DateTimeSerializer
  extends CustomSerializer[DateTime](format =>
    (
      {
        case JString(s) =>
          val zonedInstant = DateParser.parse(s, format)
          new DateTime(zonedInstant.instant, DateTimeZone.forTimeZone(zonedInstant.timezone))
        case JNull => null
      },
      { case d: DateTime =>
        JString(format.dateFormat.format(d.toDate))
      }
    )
  )

// see: http://www.joda.org/joda-time/apidocs/org/joda/time/DateMidnight.html
@deprecated(
  "The time of midnight does not exist in some time zones where the daylight saving time forward shift skips the midnight hour. Use LocalDate to represent a date without a time zone. Or use DateTime to represent a full date and time, perhaps using DateTime.withTimeAtStartOfDay() to get an instant at the start of a day. (http://www.joda.org/joda-time/apidocs/org/joda/time/DateMidnight.html)",
  since = "3.3.0"
)
case object DateMidnightSerializer
  extends CustomSerializer[DateMidnight](format =>
    (
      {
        case JString(s) =>
          val zonedInstant = DateParser.parse(s, format)
          new DateMidnight(zonedInstant.instant, DateTimeZone.forTimeZone(zonedInstant.timezone))
        case JNull => null
      },
      { case d: DateMidnight =>
        JString(format.dateFormat.format(d.toDate))
      }
    )
  )

private[ext] case class _Interval(start: Long, end: Long)
private[ext] object _Interval {
  implicit val manifest: Manifest[_Interval] = Manifest.classType(classOf[_Interval])
}
object IntervalSerializer {
  def apply(): Serializer[Interval] = new ClassSerializer(new ClassType[Interval, _Interval]() {
    def unwrap(i: _Interval)(implicit format: Formats) = new Interval(i.start, i.end)
    def wrap(i: Interval)(implicit format: Formats) = _Interval(i.getStartMillis, i.getEndMillis)
  })
}

private[ext] case class _LocalDate(year: Int, month: Int, day: Int)
private[ext] object _LocalDate {
  implicit val manifest: Manifest[_LocalDate] = Manifest.classType(classOf[_LocalDate])
}
object LocalDateSerializer {
  def apply(): Serializer[LocalDate] = new ClassSerializer(new ClassType[LocalDate, _LocalDate]() {
    def unwrap(d: _LocalDate)(implicit format: Formats) = new LocalDate(d.year, d.month, d.day)
    def wrap(d: LocalDate)(implicit format: Formats) =
      _LocalDate(d.getYear(), d.getMonthOfYear, d.getDayOfMonth)
  })
}

private[ext] case class _LocalTime(hour: Int, minute: Int, second: Int, millis: Int)
private[ext] object _LocalTime {
  implicit val manifest: Manifest[_LocalTime] = Manifest.classType(classOf[_LocalTime])
}
object LocalTimeSerializer {
  def apply(): Serializer[LocalTime] = new ClassSerializer(new ClassType[LocalTime, _LocalTime]() {
    def unwrap(t: _LocalTime)(implicit format: Formats) =
      new LocalTime(t.hour, t.minute, t.second, t.millis)
    def wrap(t: LocalTime)(implicit format: Formats) =
      _LocalTime(t.getHourOfDay, t.getMinuteOfHour, t.getSecondOfMinute, t.getMillisOfSecond)
  })
}
