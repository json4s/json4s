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
}
