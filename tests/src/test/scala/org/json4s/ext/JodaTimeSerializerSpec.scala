/*
* Copyright 2007-2011 WorldWide Conferencing, LLC
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

import java.util.TimeZone

import org.joda.time.DateTimeZone.{forTimeZone, UTC}
import org.joda.time._
import org.specs2.mutable.Specification


class NativeJodaTimeSerializerSpec extends JodaTimeSerializerSpec("Native") {
  val s: Serialization = native.Serialization
  val m: JsonMethods[_] =  native.JsonMethods
}

class JacksonJodaTimeSerializerSpec extends JodaTimeSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
  val m: JsonMethods[_] =  jackson.JsonMethods
}

/**
* System under specification for JodaTimeSerializer.
*/
abstract class JodaTimeSerializerSpec(mod: String) extends Specification {

  def s: Serialization
  def m: JsonMethods[_]

  implicit lazy val formats = s.formats(NoTypeHints) ++ JodaTimeSerializers.all

  (mod + " JodaTimeSerializer Specification") should {
    "Serialize joda time types with default format" in {
      val x = JodaTypes(new Duration(10*1000), new Instant(System.currentTimeMillis),
                        new DateTime(UTC), new DateMidnight(UTC),
                        new LocalDate(2011, 1, 16), new LocalTime(16, 52, 10), Period.weeks(3))
      val ser = s.write(x)
      s.read[JodaTypes](ser) must_== x
    }

    "DateTime and DateMidnight use configured date format 1" in {
      implicit val formats = new DefaultFormats {
        override def dateFormatter = {
          val customFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
          customFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
          customFormat
        }
      } ++ JodaTimeSerializers.all

      val x = Dates(new DateTime(2011, 1, 16, 10, 32, 0, 0, UTC), new DateMidnight(2011, 1, 16, UTC))
      val ser = s.write(x)
      ser must_== """{"dt":"2011-01-16 10:32:00Z","dm":"2011-01-16 00:00:00Z"}"""

      (m.parse(ser) \ "dt").extract[DateTime] must_== new DateTime(2011, 1, 16, 10, 32, 0, 0, UTC)
      (m.parse(ser) \ "dm").extract[DateTime] must_== new DateMidnight(2011, 1, 16, UTC)
    }

    "DateTime and DateMidnight use configured date format 2" in {

      def usTimeZone = TimeZone.getTimeZone("America/New_York")
      def usDateTimeZone = forTimeZone(usTimeZone)

      implicit val formats = new DefaultFormats {
        override def dateFormatter = {
          // non default format
          val customFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX")
          customFormat.setTimeZone(usTimeZone)
          customFormat
        }
      } ++ JodaTimeSerializers.all

      val x = Dates(new DateTime(2011, 1, 16, 10, 32, 0, 0, usDateTimeZone), new DateMidnight(2011, 1, 16, usDateTimeZone))
      val ser = s.write(x)
      ser must beMatching(
        """\{"dt":"2011-01-16 10:32:00[-+]\d{2}:\d{2}","dm":"2011-01-16 00:00:00[-+]\d{2}:\d{2}"\}""")

      (m.parse(ser) \ "dt").extract[DateTime] must_== new DateTime(2011, 1, 16, 10, 32, 0, 0, usDateTimeZone)
      (m.parse(ser) \ "dm").extract[DateTime] must_== new DateMidnight(2011, 1, 16, usDateTimeZone)
    }

    "Serialising and deserialising Date types with the default format uses UTC rather than the default local timezone" in {

      DateTimeZone.setDefault(DateTimeZone.forID("America/New_York"))

      val x = Dates(new DateTime(2011, 1, 16, 10, 32, 0, 0, UTC), new DateMidnight(2011, 1, 16, UTC))
      val ser = s.write(x)
      val deserialisedDates = s.read[Dates](ser)

      deserialisedDates must_== x
    }

    "null is serialized as JSON null" in {
      val x = JodaTypes(null, null, null, null, null, null, null)
      val ser = s.write(x)
      s.read[JodaTypes](ser) must_== x
    }
  }
}

case class JodaTypes(duration: Duration, instant: Instant, dateTime: DateTime,
                     dateMidnight: DateMidnight, localDate: LocalDate,
                     localTime: LocalTime, period: Period)

case class Dates(dt: DateTime, dm: DateMidnight)
