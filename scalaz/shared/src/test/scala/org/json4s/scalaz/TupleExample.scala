package org.json4s
package scalaz

import JsonScalaz.*
import _root_.scalaz.*
import org.scalatest.wordspec.AnyWordSpec

class TupleExample extends AnyWordSpec {
  "Parse tuple from List" in {
    val json = native.JsonParser.parse(""" [1,2,3] """)
    assert(fromJSON[Tuple3[Int, Int, Int]](json) == Success(1, 2, 3))
  }
}
