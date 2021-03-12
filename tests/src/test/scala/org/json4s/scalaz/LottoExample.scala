package org.json4s.scalaz

import scalaz._
import JsonScalaz._
import org.json4s.native
import native.JsonMethods._

import org.scalatest.wordspec.AnyWordSpec

class LottoExample extends AnyWordSpec {
  case class Winner(winnerId: Long, numbers: List[Int])
  case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winner], drawDate: Option[String])

  val json = parse(
    """{"id":5,"winning-numbers":[2,45,34,23,7,5],"winners":[{"winner-id":23,"numbers":[2,45,34,23,3,5]},{"winner-id":54,"numbers":[52,3,12,11,18,22]}]}"""
  )

  def len(x: Int): List[Int] => NonEmptyList[Error] \/ List[Int] = xs =>
    {
      if (xs.length != x) {
        Fail[List[Int]]("len", s"${xs.length} != $x")
      } else {
        Success[NonEmptyList[Error], List[Int]](xs)
      }
    }.toDisjunction

  implicit def winnerJSON: JSONR[Winner] =
    Winner.applyJSON(field[Long]("winner-id"), validate[List[Int]]("numbers") >==> len(6))

  implicit def lottoJSON: JSONR[Lotto] =
    Lotto.applyJSON(
      field[Long]("id"),
      validate[List[Int]]("winning-numbers") >==> len(6),
      field[List[Winner]]("winners"),
      field[Option[String]]("draw-date")
    )

  val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
  val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5), winners, None)

  "LottoExample" in {
    assert(fromJSON[Lotto](json) == Success(lotto))
  }
}
