package com.tt.json4s

class JacksonLottoExample extends LottoExample[JValue]("Jackson") with jackson.JsonMethods {
  import LottoExample._
  implicit val formats: Formats = DefaultFormats
  def extractWinner(jv: JValue): Winner = jv.extract[Winner]

  def extractLotto(jv: JValue): Lotto = jv.extract[Lotto]
}
