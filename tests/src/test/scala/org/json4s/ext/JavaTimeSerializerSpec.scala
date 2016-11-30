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

package org.json4s.ext

import java.time._

import org.json4s._
import org.specs2.mutable.Specification


object NativeJavaTimeSerializerSpec extends JavaTimeSerializerSpec("Native") {
  val s: Serialization = native.Serialization
}

object JacksonJavaTimeSerializerSpec extends JavaTimeSerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}

/**
* System under specification for JavaTimeSerializer.
*/
abstract class JavaTimeSerializerSpec(mod: String) extends Specification {

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
          new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
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


