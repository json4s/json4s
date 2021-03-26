package org.json4s.native

import org.scalatest.wordspec.AnyWordSpec
import org.json4s._
import org.json4s.native.Serialization.{read, write => swrite}
import org.json4s.native
import java.util.{GregorianCalendar, Date}
import java.sql.Timestamp

class MapSerializationExamples extends AnyWordSpec {
  implicit val formats: Formats = native.Serialization.formats(NoTypeHints)

  "Map with Symbol key" in {
    val pw = Map[Symbol, String](Symbol("a") -> "hello", Symbol("b") -> "world")
    val ser = swrite(pw)
    assert(ser == """{"a":"hello","b":"world"}""")
    assert(read[Map[Symbol, String]](ser) == pw)
  }

  "Map with Int key" in {
    val pw = Map[Int, String](1 -> "hello", 2 -> "world")
    val ser = swrite(pw)
    assert(ser == """{"1":"hello","2":"world"}""")
    assert(read[Map[Int, String]](ser) == pw)
  }

  "Map with Long key" in {
    val pw = Map[Long, String](1L -> "hello", 2L -> "world")
    val ser = swrite(pw)
    assert(ser == """{"1":"hello","2":"world"}""")
    assert(read[Map[Long, String]](ser) == pw)
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
    assert(ser == { """{"""" + f2013 + """":"hello","""" + f2014 + """":"world"}""" })
    assert(read[Map[Date, String]](ser) == pw)
  }

  "Map with Timestamp key" in {
    val t2013 = new Timestamp(1356998400)
    val t2014 = new Timestamp(1388534400)
    val pw: Map[Timestamp, String] = Map(t2013 -> "hello", t2014 -> "world")
    val ser = swrite(pw)

    val f2013 = formats.dateFormat.format(t2013)
    val f2014 = formats.dateFormat.format(t2014)
    assert(ser == { """{"""" + f2013 + """":"hello","""" + f2014 + """":"world"}""" })
    assert(read[Map[Timestamp, String]](ser) == pw)
  }

  "Map with custom key and no custom serializer -- should suggest CustomKeySerializer implementation" in {
    val pw = Map[KeyWithInt, String](KeyWithInt(1) -> "hello", KeyWithInt(2) -> "world")
    try {
      swrite(pw)
      fail()
    } catch {
      case thrown: MappingException =>
        assert(thrown.getMessage.contains("Consider implementing a CustomKeySerializer"))
        assert(thrown.getMessage.contains("KeyWithInt"))
    }
  }

  "Map with custom key and custom key serializer" in {
    val serializer = new CustomKeySerializer[KeyWithInt](format =>
      (
        { case s: String => KeyWithInt(s.toInt) },
        { case k: KeyWithInt => k.id.toString }
      )
    )
    implicit val formats: Formats = native.Serialization.formats(NoTypeHints) + serializer

    val pw = Map[KeyWithInt, String](KeyWithInt(1) -> "hello", KeyWithInt(2) -> "world")
    val ser = swrite(pw)
    assert(ser == """{"1":"hello","2":"world"}""")
    assert(read[Map[KeyWithInt, String]](ser) == pw)
  }

  "case class with custom map" in {
    val pw = PlayerWithCustomMap("zortan", Map("2013" -> "zortan13", "2014" -> "zortan14"))
    val ser = swrite(pw)
    val s: String = """{"name":"zortan","aliasByYear":{"2013":"zortan13","2014":"zortan14"}}"""
    assert(ser == s)
    val deser = read[PlayerWithCustomMap](ser)
    assert(deser == pw)
  }
}

case class KeyWithInt(id: Int)

case class PlayerWithCustomMap(name: String, aliasByYear: Map[String, String])
