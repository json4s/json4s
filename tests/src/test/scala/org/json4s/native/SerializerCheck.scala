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
  property(p2) = {
    implicit val arbJson = ArbitraryJson4s.arbJValueDouble
    forAll { json: JValue => serDeserAreInverses(json, false) }
  }

val p2 =
"""
In *decimal mode* any json document can be serialized into a string and
deserialized into an equivalent json document.
""".trim
  // property(p2) = {
  //   implicit val arbJson = ArbitraryJson4s.arbJValueDecimal
  //   forAll { json: JValue => serDeserAreInverses(json, true) }
  // }

  def serDeserAreInverses(json: JValue, decimalMode: Boolean): Boolean = {
    type SerType = List[JValue]
    val lst1: SerType = List(json)
    val lst2: SerType = read[SerType](
      write[SerType](List(json)),
      decimalMode
    )
    lst1 == lst2
  }
}
