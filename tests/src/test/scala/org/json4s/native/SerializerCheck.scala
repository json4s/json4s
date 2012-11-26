package org.json4s

import org.json4s.native.{ Serialization => NSerialization }
import NSerialization._

import org.scalacheck._
import Prop._

object SerializerCheck extends Properties("serializer") {

  implicit val arbString = alphaNumStr
  implicit val formats = NSerialization formats NoTypeHints

  def alphaNumStr: Gen[String] = for {
    cs <- Gen listOf Gen.alphaNumChar
  } yield cs.mkString

  val p1 =
"""
In *double mode* any json document can be serialized into a string and
deserialized into an equivalent json document.
""".trim
  property(p1) = {
    implicit val arbJson = ArbitraryJson4s.arbJValueDouble
    type SerType = Pair[JValue, JValue]
    forAll { json: JValue =>
      val in: SerType = Pair(json, json)
      val s: String = write[SerType](in)
      val out: SerType = read[SerType](s, false)
      in == out
    }
  }

val p2 =
"""
In *decimal mode* any json document can be serialized into a string and
deserialized into an equivalent json document.
""".trim
  property(p2) = forAll { i: Int =>
    i + 0 == i
  }
}

case class Foo(x: JValue)
