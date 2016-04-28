package org.json4s

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary

object ArbitraryJson4s {
  //TODO allow for larger trees
  //TODO use ScalaCheck's size param
  val MaxSize = 1

  val genJValueDouble: Gen[JValue] = new Nodes(false).genJValue
  val arbJValueDouble: Arbitrary[JValue] = Arbitrary(genJValueDouble)

  val genJValueDecimal: Gen[JValue] = new Nodes(true).genJValue
  val arbJValueDecimal: Arbitrary[JValue] = Arbitrary(genJValueDecimal)

  // def genJDecimal: Gen[JDecimal] = { //TODO use better BigDecimal generation
  //   val gen: Gen[JDecimal] = for {
  //     d <- arbitrary[BigDecimal]
  //   } yield JDecimal(d)
  //   gen suchThat {
  //     case JDecimal(bd) => try {
  //       bd.underlying.toBigIntegerExact
  //       false
  //     }
  //     catch {
  //       case _ => true
  //     }
  //   } suchThat {
  //     case JDecimal(bd) => try {
  //       BigDecimal(bd.toString)
  //       true
  //     }
  //     catch {
  //       case _ => false
  //     }
  //   }
  // }
  def genJDecimal: Gen[JDecimal] = for {
    d <- arbitrary[Double]
  } yield JDecimal(BigDecimal(d))
  def arbJDecimal: Arbitrary[JDecimal] = Arbitrary(genJDecimal)

  def genJDouble: Gen[JDouble] = for {
    d <- arbitrary[Double]
  } yield JDouble(d)
  def arbJDouble: Arbitrary[JDouble] = Arbitrary(genJDouble)

  def genJString: Gen[JString] = for {
    s <- arbitrary[String]
  } yield JString(s)
  def arbJString: Arbitrary[JString] = Arbitrary(genJString)

  def genJBool: Gen[JBool] = for {
    b <- arbitrary[Boolean]
  } yield JBool(b)
  def arbJBool: Arbitrary[JBool] = Arbitrary(genJBool)

  def genJInt: Gen[JInt] = for {
    l <- arbitrary[Long]
  } yield JInt(l)
  def arbJInt: Arbitrary[JInt] = Arbitrary(genJInt)

  def genJNull: Gen[JNull.type] = Gen const JNull
  def arbJNull: Arbitrary[JNull.type] = Arbitrary(genJNull)

  def genJNothing: Gen[JNothing.type] = Gen const JNothing
  def arbJNothing: Arbitrary[JNothing.type] = Arbitrary(genJNothing)

  class Nodes(decimalMode: Boolean) {

    implicit val arb = Arbitrary(genJValue)
    private[this] implicit val arbObj = arbGenJObject

    def genJValue: Gen[JValue] = Gen frequency (
      (75, genJValueSansFloat),
      (25, if (decimalMode) genJDecimal else genJDouble)
    )

    def genJValueSansFloat: Gen[JValue] = Gen frequency (
      (15, genJNull),
      (15, genJString),
      (15, genJInt),
      (15, genJBool),
      (30, genJObject),
      (30, genJArray)
    )

    def genJField: Gen[JField] = for {
      s <- arbitrary[String]
      v <- arbitrary[JValue]
    } yield (s, v)
    def arbGenJField: Arbitrary[JField] = Arbitrary(genJField)

    def genJObject: Gen[JObject] = for {
      lst: List[JField] <- containerOfN[List, JField](MaxSize, genJField)
    } yield JObject(lst)
    def arbGenJObject: Arbitrary[JObject] = Arbitrary(genJObject)

    def genJArray: Gen[JArray] = for {
      obj: JObject <- arbitrary[JObject]
    } yield JArray {
      val jvals: List[JValue] = obj.fold(List.empty[JValue]) {
        (acc, el) => el :: acc
      }
      jvals
    }
    def arbGenJArray: Arbitrary[JArray] = Arbitrary(genJArray)
  }
}
