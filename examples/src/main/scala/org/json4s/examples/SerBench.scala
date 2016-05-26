package org.json4s
package examples

import java.util.Date
import org.json4s._
import java.io._
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.atomic.AtomicLong

object SerBench extends Benchmark {

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

  val classes = List(classOf[Project], classOf[Team], classOf[Employee], classOf[Language])
  val counter = new AtomicLong(0)
  def project = {
    val c = counter.incrementAndGet()
    Project("test"+c, new Date, Some(Language("Scala"+c, 2.75+c)), List(
      Team("QA"+c, List(Employee("John Doe"+c, 5+c.toInt), Employee("Mike"+c, 3+c.toInt))),
      Team("Impl"+c, List(Employee("Mark"+c, 4+c.toInt), Employee("Mary"+c, 5+c.toInt), Employee("Nick Noob"+c, 1+c.toInt)))))
  }

  val projJson = Extraction.decompose(project)(DefaultFormats)

  val projectJValue = {
    projJson merge (JObject(JField("name", JString("test"+counter.incrementAndGet()))))
  }



  val mapper = new ObjectMapper()

  def main(args: Array[String]): Unit = {
    println("## Serialization  ")

    val str = project.toString

    // def strr = str
    // benchmark("Java serialization (ser)") { serialize(project) }
    // benchmark("Java noop") { strr }
    // benchmark("Java toString (ser)") { project.toString }
    // println()

    // println("### Jackson with Scala module")
    // benchmark("Jackson serialization (full)") { mapper.readValue(mapper.writeValueAsString(project), classOf[Project])}
    // benchmark("Jackson serialization (ser)") { mapper.writeValueAsString(project) }
    // val ser3 = mapper.writeValueAsString(project)
    // benchmark("Jackson (deser)") { mapper.readValue(ser3, classOf[Project]) }
    // parseBenchmark("Jackson AST (parse)") { mapper.readValue(json, classOf[JsonNode]) }
    // val jn = mapper.readValue(json, classOf[JsonNode])
    // benchmark("Jackson AST (ser)") { mapper.writeValueAsString(jn) }
    // println()

    println("### Json4s direct AST")
    parseBenchmark("json4s-native AST (parse)") { native.JsonMethods.parse(json) }
    parseBenchmark("json4s-jackson AST (parse)") { jackson.JsonMethods.parse(json)}
    benchmark("json4s-native AST (ser)") { native.JsonMethods.compact(native.JsonMethods.render(projectJValue)) }
    benchmark("json4s-jackson AST (ser)") { jackson.JsonMethods.compact(projectJValue) }
    println()

    println("### Custom serializer")
    new Bench()(DefaultFormats + new ProjectSerializer)
    println()

    println("### No type hints")
    new Bench()(DefaultFormats)
    println()

    println("### Short type hints")
    new Bench()(native.Serialization.formats(ShortTypeHints(classes)))
    println()

    println("### Full type hints")
    new Bench()(DefaultFormats + FullTypeHints(classes))
    println()
  }

  def benchmark(name: String)(f: => Any) = run(name, 120000, 20000)(f)
  def parseBenchmark(name: String)(f: => Any) = run(name, 150000, 150000)(f)

  def serialize(project: Project) = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(project)
    baos.toByteArray
  }

  def deserialize(array: Array[Byte]) =
        new ObjectInputStream(new ByteArrayInputStream(array)).readObject.asInstanceOf[Project]

  class Bench(implicit formats: Formats) {
//    benchmark("Java serialization (full)") { deserialize(serialize(project)) }

    benchmark("json4s-native (full)") { native.Serialization.read[Project]( native.Serialization.write(project)) }
    benchmark("json4s-jackson (full)") { jackson.Serialization.read[Project]( jackson.Serialization.write(project)) }
    benchmark("json4s-native (ser)") { native.Serialization.write(project) }
    benchmark("json4s-jackson (ser)") { jackson.Serialization.write(project) }
//    val ser1 = serialize(project)
    val ser2 = native.Serialization.write(project)

//    benchmark("Java serialization (deser)") { deserialize(ser1) }

    benchmark("json4s-native (deser)") { native.Serialization.read[Project](ser2) }
    benchmark("json4s-jackson (deser)") { jackson.Serialization.read[Project](ser2) }

    benchmark("json4s-native old pretty") { native.Serialization.writePrettyOld(project) }
//    benchmark("json4s-jackson old pretty") { jackson.Serialization.writePrettyOld(project) }





  }

  class ProjectSerializer extends CustomSerializer[org.json4s.examples.Project](implicit formats => ({
    case jv @ JObject(("name", JString(name)) :: ("startDate", JString(startDate)) :: _) =>
      val lang = (jv \ "lang") match {
        case JNothing => None
        case lj =>
          Some(Language((lj \ "name").extract[String], (lj \ "version").extract[Double]))
      }
      org.json4s.examples.Project(
        name = name,
        startDate = formats.dateFormat.parse(startDate).get,
        lang = lang,
        teams = (jv \ "teams") match {
          case JArray(jvs) => jvs map { tm =>
            Team(
              (tm \ "role").extract[String],
              (tm \ "members") match {
                case JArray(mems) => mems map { mem =>
                  Employee((mem \ "name").extract[String], (mem \ "experience").extract[Int])
                }
                case _ => Nil
              }
            )
          }
          case _ => Nil
        }
      )
  },{
    case pr: org.json4s.examples.Project => {
      import JsonDSL._

      val lang = pr.lang map { l =>
        ("name" -> l.name) ~
        ("version" -> l.version)
      } getOrElse JNothing

      val teams = pr.teams map { team =>
        ("role" -> team.role) ~
        ("members" -> (team.members map { mem =>
          ("name" -> mem.name) ~
          ("experience" -> mem.experience)
        }))
      }

      ("name" -> pr.name) ~
      ("startDate" -> formats.dateFormat.format(pr.startDate)) ~
      ("lang" -> lang) ~
      ("teams" -> teams)
    }
  }))

}

case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team]) extends Serializable
case class Language(name: String, version: Double) extends Serializable
case class Team(role: String, members: List[Employee]) extends Serializable
case class Employee(name: String, experience: Int) extends Serializable
