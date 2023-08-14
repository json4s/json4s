package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.JsonMethods.parse

class JsonParserSpecJVM extends AnyWordSpec {
  "Parsing is thread safe" in {
    import java.util.concurrent._

    val json = """{
      "person": {
        "name": "Joe",
        "age": 35,
        "spouse": {
          "person": {
            "name": "Marilyn",
            "age": 33
          }
        }
      }
    }"""
    val executor = Executors.newFixedThreadPool(100)
    try {
      val results =
        Seq.fill(101) { executor.submit(new Callable[JValue] { def call = parse(json) }) }.toList.map(_.get)
      assert(results.zip(results.tail).forall(pair => pair._1 == pair._2))
    } finally {
      executor.shutdown()
    }
  }
}
