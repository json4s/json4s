package org.json4s

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop
import org.json4s.DefaultJsonFormats._

class CaseClassJsonFormatSpec extends Properties("case class JsonFormat") {
  private[this] val format1: JsonFormat[CaseClass22] =
    JsonFormat.format22(CaseClass22.apply, CaseClass22.unapply(_: CaseClass22).get)(
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "10",
      "11",
      "12",
      "13",
      "14",
      "15",
      "16",
      "17",
      "18",
      "19",
      "20",
      "21",
      "22"
    )

  private[this] implicit val arbitrary: Arbitrary[CaseClass22] =
    Arbitrary(Gen.resultOf(CaseClass22.tupled))

  property("case class JsonFormat") = Prop.forAll { (a: CaseClass22) =>
    format1.read(format1.write(a)) == a
  }
}
