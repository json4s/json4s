package org.json4s

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop
import org.json4s.DefaultJsonFormats._

class CaseClassJsonFormatSpec extends Properties("case class JsonFormat") {
  private[this] val writerAuto: Writer[CaseClass22] =
    Writer.writerAuto(Tuple.fromProductTyped[CaseClass22])

  private[this] val reader: Reader[CaseClass22] =
    Reader.reader(CaseClass22.apply)(
      "a1",
      "a2",
      "a3",
      "a4",
      "a5",
      "a6",
      "a7",
      "a8",
      "a9",
      "a10",
      "a11",
      "a12",
      "a13",
      "a14",
      "a15",
      "a16",
      "a17",
      "a18",
      "a19",
      "a20",
      "a21",
      "a22"
    )

  private[this] val format1: JsonFormat[CaseClass22] =
    JsonFormat.format22(CaseClass22.apply, Tuple.fromProductTyped(_: CaseClass22))(
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
    Arbitrary(Gen.resultOf(CaseClass22.apply))

  property("case class JsonFormat") = Prop.forAll { (a: CaseClass22) =>
    format1.read(format1.write(a)) == a
  }

  property("writerAuto") = Prop.forAll { (a: CaseClass22) =>
    reader.read(writerAuto.write(a)) == a
  }
}
