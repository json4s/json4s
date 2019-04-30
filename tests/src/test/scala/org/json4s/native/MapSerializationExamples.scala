package org.json4s.native

import org.specs2.mutable.Specification
import org.json4s._
import org.json4s.native.Serialization.{read, write => swrite}
import org.json4s.native
import java.util.{GregorianCalendar, Date}
import java.sql.Timestamp

class MapSerializationExamples extends Specification {
  implicit val formats = native.Serialization.formats(NoTypeHints)

  "Map with Symbol key" in {
    val pw = Map[Symbol, String](Symbol("a") -> "hello", Symbol("b") -> "world")
    val ser = swrite(pw)
    ser must_== """{"a":"hello","b":"world"}"""
    read[Map[Symbol, String]](ser) must_== pw
  }

  "Map with Int key" in {
    val pw = Map[Int, String](1 -> "hello", 2 -> "world")
    val ser = swrite(pw)
    ser must_== """{"1":"hello","2":"world"}"""
    read[Map[Int, String]](ser) must_== pw
  }

  "Map with Long key" in {
    val pw = Map[Long, String](1L -> "hello", 2L -> "world")
    val ser = swrite(pw)
    ser must_== """{"1":"hello","2":"world"}"""
    read[Map[Long, String]](ser) must_== pw
  }

  "Map with Date key" in {
    //months are zero indexed
    val gc = new GregorianCalendar(2013, 0, 1)
    val d2013 = gc.getTime
    gc.set(2014, 0, 1)
    val d2014 = gc.getTime
    val pw = Map[Date, String](d2013 -> "hello", d2014 -> "world")
    val ser = swrite(pw)
    val f2013 = formats.dateFormat.format(d2013)
    val f2014 = formats.dateFormat.format(d2014)
    ser must_== """{"""" + f2013 + """":"hello","""" + f2014 + """":"world"}"""
    read[Map[Date, String]](ser) must_== pw
  }

  "Map with Timestamp key" in {
    val t2013 = new Timestamp(1356998400)
    val t2014 = new Timestamp(1388534400)
    val pw = Map(t2013 -> "hello", t2014 -> "world")
    val ser = swrite(pw)

    val f2013 = formats.dateFormat.format(t2013)
    val f2014 = formats.dateFormat.format(t2014)
    ser must_== """{"""" + f2013 + """":"hello","""" + f2014 + """":"world"}"""
    read[Map[Timestamp, String]](ser) must_== pw
  }

  "Map with custom key and no custom serializer -- should suggest CustomKeySerializer implementation" in {
    val pw = Map[KeyWithInt, String](KeyWithInt(1) -> "hello", KeyWithInt(2) -> "world")
    val thrown = swrite(pw) must throwA[MappingException]
    thrown.message must contain("Consider implementing a CustomKeySerializer")
    thrown.message must contain("KeyWithInt")
  }

  "Map with custom key and custom key serializer" in {
    val serializer = new CustomKeySerializer[KeyWithInt](format => (
      { case s: String => KeyWithInt(s.toInt)},
      { case k: KeyWithInt => k.id.toString }
    ))
    implicit val formats = native.Serialization.formats(NoTypeHints) + serializer

    val pw = Map[KeyWithInt, String](KeyWithInt(1) -> "hello", KeyWithInt(2) -> "world")
    val ser = swrite(pw)
    ser must_== """{"1":"hello","2":"world"}"""
    read[Map[KeyWithInt, String]](ser) must_== pw
  }

  "case class with custom map" in {
    val pw = PlayerWithCustomMap("zortan", Map("2013" -> "zortan13", "2014" -> "zortan14"))
    val ser = swrite(pw)
    val s: String = """{"name":"zortan","aliasByYear":{"2013":"zortan13","2014":"zortan14"}}"""
    ser must_== s
    val deser = read[PlayerWithCustomMap](ser)
    deser must_== pw
  }
}

case class KeyWithInt(id: Int)

case class PlayerWithCustomMap(name: String, aliasByYear: Map[String, String])
