package org.json4s
package scalaz

import _root_.scalaz._
import JsonScalaz._

import org.specs2.mutable.Specification

object TupleExample extends Specification {
  "Parse tuple from List" in {
    val json = native.JsonParser.parse(""" [1,2,3] """)
    fromJSON[Tuple3[Int, Int, Int]](json) must_== Success(1, 2, 3)
  }


  "Parse case class from tuple with xmap" in {

    case class CC(i: Int, s: String)

    implicit val ccJSON = JSON[(Int, String)].xmap[CC](
      tp => CC(tp._1, tp._2),
      cc => (cc.i, cc.s)
    )

    val json = native.JsonParser.parse(""" [1,"foo"] """)
    fromJSON[CC](json) must_== Success(CC(1, "foo"))


    val json2 = native.JsonParser.parse(""" [1,2] """)
    fromJSON[CC](json2) must beLike[Result[CC]] {
      case Failure(xs) => xs.size must_== 1
    }

  }
}
