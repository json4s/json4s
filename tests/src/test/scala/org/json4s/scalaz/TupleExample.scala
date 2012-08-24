package org.json4s.scalaz

import scalaz._
import Scalaz._
import JsonScalaz._
import org.json4s._
import native._

import org.specs.Specification

object TupleExample extends Specification {
  "Parse tuple from List" in {
    val json = JsonParser.parse(""" [1,2,3] """)
    fromJSON[Tuple3[Int, Int, Int]](json) mustEqual Success(1, 2, 3)
  }
}
