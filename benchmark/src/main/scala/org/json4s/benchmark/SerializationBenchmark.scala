package org.json4s
package benchmark

import java.util.Date
import com.fasterxml.jackson.databind.{JsonNode, DeserializationFeature, ObjectMapper}
import java.util.concurrent.atomic.AtomicInteger

class Json4sBenchmark extends SimpleScalaBenchmark {

  private[this] val counter = new AtomicInteger(0)
  private[this] var project: Project = _
  private[this] var projectJson: String = _
  private[this] var projectJValue: JValue = _
  private implicit val formats: Formats = DefaultFormats
  private[this] val glossaryJson = """
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

  private[this] val mapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false)



  override def setUp(): Unit = {
    val c = counter.incrementAndGet()
    project = Project("test"+c, new Date, Some(Language("Scala"+c, 2.75+c)), List(
          Team("QA"+c, List(Employee("John Doe"+c, 5+c), Employee("Mike"+c, 3+c))),
          Team("Impl"+c, List(Employee("Mark"+c, 4+c), Employee("Mary"+c, 5+c), Employee("Nick Noob"+c, 1+c)))))
    projectJson = jackson.Serialization.write(project)
    projectJValue = Extraction.decompose(project)
  }

  def timeJacksonFullCircle(reps: Int) = repeat(reps) {
    mapper.readValue(mapper.writeValueAsString(project), classOf[Project])
  }

  def timeJacksonSerialization(reps: Int) = repeat(reps) { mapper.writeValueAsString(project) }

  def timeJacksonDeserialization(reps: Int) = repeat(reps) { mapper.readValue(projectJson, classOf[Project]) }

  def timeJacksonParsing(reps: Int) = repeat(reps) { mapper.readValue(glossaryJson, classOf[JsonNode]) }

  def timeJson4sDecomposition(reps: Int) = repeat(reps) { Extraction.decompose(project) }

  def timeJson4sNativeFullCircle(reps: Int) = repeat(reps) {
    native.Serialization.read[Project](native.Serialization.write(project))
  }

  def timeJson4sNativeSerialization(reps: Int) = repeat(reps) {  native.Serialization.write(project) }

  def timeJson4sNativeDeserialization(reps: Int) = repeat(reps) { native.Serialization.read[Project](projectJson) }

  def timeJson4sNativeParsing(reps: Int) = repeat(reps) { native.JsonMethods.parse(glossaryJson) }

  def timeJson4sNativeJValueWriting(reps: Int) = repeat(reps) {
    native.JsonMethods.compact(native.JsonMethods.render(projectJValue))
  }

  def timeJson4sJacksonFullCircle(reps: Int) = repeat(reps) {
    jackson.Serialization.read[Project](jackson.Serialization.write(project))
  }

  def timeJson4sJacksonSerialization(reps: Int) = repeat(reps) { jackson.Serialization.write(project) }

  def timeJson4sJacksonDeserialization(reps: Int) = repeat(reps) { jackson.Serialization.read[Project](projectJson) }

  def timeJson4sJacksonParsing(reps: Int) = repeat(reps) { jackson.JsonMethods.parse(glossaryJson) }

  def timeJson4sJacksonJValueWriting(reps: Int) = repeat(reps) {
    jackson.JsonMethods.compact(jackson.JsonMethods.render(projectJValue))
  }

}
