package org.json4s

import org.json4s.native.Document

class NativeLottoExample extends LottoExample[Document]("Native") with native.JsonMethods {
  import LottoExample.*
  implicit val formats: Formats = DefaultFormats
  def extractWinner(jv: JValue): Winner = jv.extract[Winner]

  def extractLotto(jv: JValue): Lotto = jv.extract[Lotto]
}
