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

import org.joda.time._

import org.specs.Specification

import common._
import json.Serialization.{read, write => swrite}


/**
 * System under specification for JodaTimeSerializer.
 */
object JodaTimeSerializerSpec extends Specification("JodaTimeSerializer Specification") {

  implicit val formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all

  "Serialize joda time types" in {
    val x = JodaTypes(new Duration(10*1000), new Instant(System.currentTimeMillis),
                      new DateTime, new DateMidnight, new Interval(1000, 50000),
                      new LocalDate(2011, 1, 16), new LocalTime(16, 52, 10), Period.weeks(3))
    val ser = swrite(x)
    read[JodaTypes](ser) mustEqual x
  }

  "DateTime and DateMidnight use configured date format" in {
    implicit val formats = new net.liftweb.json.DefaultFormats {                      
      override def dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
    } ++ JodaTimeSerializers.all

    val x = Dates(new DateTime(2011, 1, 16, 10, 32, 0, 0, DateTimeZone.UTC), new DateMidnight(2011, 1, 16, DateTimeZone.UTC))
    val ser = swrite(x)
    ser mustEqual """{"dt":"2011-01-16 10:32:00Z","dm":"2011-01-16 00:00:00Z"}"""
  }

  "null is serialized as JSON null" in {
    val x = JodaTypes(null, null, null, null, null, null, null, null)
    val ser = swrite(x)
    read[JodaTypes](ser) mustEqual x
  }
}

case class JodaTypes(duration: Duration, instant: Instant, dateTime: DateTime, 
                     dateMidnight: DateMidnight, interval: Interval, localDate: LocalDate,
                     localTime: LocalTime, period: Period)

case class Dates(dt: DateTime, dm: DateMidnight)
