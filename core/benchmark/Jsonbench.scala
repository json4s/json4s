/**
 * Following libs are required to compile and run the benchmark:
 * - jackson-core-asl-1.4.1.jar
 * - jackson-mapper-asl-1.4.1.jar
 * - lift-json-???.jar
 */
object Jsonbench extends Benchmark {
  import scala.util.parsing.json.JSON
  import org.codehaus.jackson._
  import org.codehaus.jackson.map._
  import net.liftweb.json.JsonParser

  def main(args: Array[String]) = {
    benchmark("Scala std") { JSON.parse(json) }
    val mapper = new ObjectMapper
    benchmark("Jackson") { mapper.readValue(json, classOf[JsonNode]) }
    benchmark("lift-json") { JsonParser.parse(json) }
  }

  def benchmark(name: String)(f: => Any) = run(name, 50000, 50000)(f)

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
