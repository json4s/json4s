package org.json4s
package scalaz

import _root_.scalaz._
import Scalaz._
import JsonScalaz._

import org.scalatest.FunSpec

class TupleExample extends FunSpec {
  it("Parse tuple from List") {
    val json = native.JsonParser.parse(""" [1,2,3] """)
    assert(fromJSON[Tuple3[Int, Int, Int]](json) == Success(1, 2, 3))
  }
}
