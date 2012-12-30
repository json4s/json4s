package org.json4s
package examples

import java.util.Date
import org.json4s._
import java.io._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object SerBench extends Benchmark {
  val classes = List(classOf[Project], classOf[Team], classOf[Employee], classOf[Language])
  val project = Project("test", new Date, Some(Language("Scala", 2.75)), List(
    Team("QA", List(Employee("John Doe", 5), Employee("Mike", 3))),
    Team("Impl", List(Employee("Mark", 4), Employee("Mary", 5), Employee("Nick Noob", 1)))))

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]) {
    println("** No type hints")
    new Bench()(native.Serialization.formats(NoTypeHints))
    println("** Short type hints")
    new Bench()(native.Serialization.formats(ShortTypeHints(classes)))
    println("** Full type hints")
    new Bench()(native.Serialization.formats(FullTypeHints(classes)))
  }

  class Bench(implicit formats: Formats) {
//    benchmark("Java serialization (full)") { deserialize(serialize(project)) }
    benchmark("Jackson serialization (full)") { mapper.readValue(mapper.writeValueAsString(project), classOf[Project])}
    benchmark("json4s-native (full)") { native.Serialization.read[Project]( native.Serialization.write(project)) }
    benchmark("json4s-jackson (full)") { jackson.Serialization.read[Project]( jackson.Serialization.write(project)) }

    benchmark("Java serialization (ser)") { serialize(project) }
    benchmark("Jackson serialization (ser)") { mapper.writeValueAsString(project) }
    benchmark("json4s-native (ser)") { native.Serialization.write(project) }
    benchmark("json4s-jackson (ser)") { jackson.Serialization.write(project) }
    val ser1 = serialize(project)
    val ser2 = native.Serialization.write(project)
    val ser3 = mapper.writeValueAsString(project)
//    benchmark("Java serialization (deser)") { deserialize(ser1) }
    benchmark("Jackson (deser)") { mapper.readValue(ser3, classOf[Project]) }
    benchmark("json4s-native (deser)") { native.Serialization.read[Project](ser2) }
    benchmark("json4s-jackson (deser)") { jackson.Serialization.read[Project](ser2) }

    def benchmark(name: String)(f: => Any) = run(name, 20000, 20000)(f)

    def deserialize(array: Array[Byte]) =
      new ObjectInputStream(new ByteArrayInputStream(array)).readObject.asInstanceOf[Project]

    def serialize(project: Project) = {
      val baos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(project)
      baos.toByteArray
    }
  }

}

case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team]) extends Serializable
case class Language(name: String, version: Double) extends Serializable
case class Team(role: String, members: List[Employee]) extends Serializable
case class Employee(name: String, experience: Int) extends Serializable
