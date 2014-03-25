package org.json4s.scalaz

import scalaz._
import Scalaz._
import JsonScalaz._
import org.json4s.native
import native.JsonMethods._

import org.specs2.mutable.Specification

object LottoExample extends Specification {
  case class Winner(winnerId: Long, numbers: List[Int])
  case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winner], drawDate: Option[String])

  val json = parse("""{"id":5,"winning-numbers":[2,45,34,23,7,5],"winners":[{"winner-id":23,"numbers":[2,45,34,23,3,5]},{"winner-id":54,"numbers":[52,3,12,11,18,22]}]}""")

  // Lotto line must have exactly 6 numbers
//  def len(x: Int) = (xs: List[Int]) =>
//    if (xs.length != x) Fail("len", s"${xs.length} != $x") else xs.success

  // FIXME enable when 2.8 no longer supported, 2.9 needs: import Validation.Monad._
/*
  // Note 'apply _' is not needed on Scala 2.8.1 >=
  implicit def winnerJSON: JSONR[Winner] =
    Winner.applyJSON(field("winner-id"), validate[List[Int]]("numbers") >=> len(6) apply _)

  implicit def lottoJSON: JSONR[Lotto] =
    Lotto.applyJSON(field("id")
                  , validate[List[Int]]("winning-numbers") >=> len(6) apply _
                  , field("winners")
                  , field("draw-date"))

  val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
  val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5), winners, None)

  fromJSON[Lotto](json) must_== Success(lotto)
  */
}

