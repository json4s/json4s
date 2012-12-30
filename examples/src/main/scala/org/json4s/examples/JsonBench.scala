package org.json4s
package examples

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

/**
* Following libs are required to compile and run the benchmark:
*/
object Jsonbench extends Benchmark {
  import scala.util.parsing.json.JSON

  def main(args: Array[String]) = {
    benchmark("Scala std") { JSON.parse(json) }
    val mapper = new ObjectMapper
    benchmark("Jackson") { mapper.readValue(json, classOf[JsonNode]) }
    benchmark("json4s-native") { native.JsonMethods.parse(json) }
    benchmark("json4s-jackson") { jackson.JsonMethods.parse(json)}
  }

  def benchmark(name: String)(f: => Any) = run(name, 150000, 150000)(f)

  val json = """
{
  "glossary": {
    "title": "example glossary",
    "GlossDiv": {
      "title": "S",
      "GlossList": {
        "GlossEntry": {
          "ID": "SGML",
          "SortAs": "SGML",
          "GlossTerm": "Standard Generalized Markup Language",
          "Acronym": "SGML",
          "Abbrev": "ISO 8879:1986",
          "GlossDef": {
            "para": "A meta-markup language, used to create markup languages such as DocBook.",
            "GlossSeeAlso": ["GML", "XML"]
          },
          "GlossSee": "markup"
        }
      }
    }
  }
}
"""
}