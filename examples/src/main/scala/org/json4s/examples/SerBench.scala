package org.json4s
package examples

import java.util.Date
import org.json4s._
import java.io._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.util.concurrent.atomic.AtomicLong

object SerBench extends Benchmark {



  val classes = List(classOf[Project], classOf[Team], classOf[Employee], classOf[Language])
  val counter = new AtomicLong(0)
  def project = {
    val c = counter.incrementAndGet()
    Project("test"+c, new Date, Some(Language("Scala"+c, 2.75+c)), List(
      Team("QA"+c, List(Employee("John Doe"+c, 5+c.toInt), Employee("Mike"+c, 3+c.toInt))),
      Team("Impl"+c, List(Employee("Mark"+c, 4+c.toInt), Employee("Mary"+c, 5+c.toInt), Employee("Nick Noob"+c, 1+c.toInt)))))
  }

  val projJson = Extraction.decompose(project)(DefaultFormats)

  def projectJValue = {
    projJson merge (JObject(JField("name", JString("test"+counter.incrementAndGet()))))
  }



  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]) {
    benchmark("Jackson serialization (full)") { mapper.readValue(mapper.writeValueAsString(project), classOf[Project])}
    benchmark("Java serialization (ser)") { serialize(project) }
    benchmark("Jackson serialization (ser)") { mapper.writeValueAsString(project) }
    val ser3 = mapper.writeValueAsString(project)
    benchmark("Jackson (deser)") { mapper.readValue(ser3, classOf[Project]) }
    val str = project.toString
    def strr = str
    benchmark("Java noop") { strr }
    benchmark("Java toString (ser)") { project.toString }
    benchmark("json4s-native JValue toString (ser)") { native.JsonMethods.compact(native.JsonMethods.render(projectJValue)) }
    benchmark("json4s-jackson JValue toString (ser)") { jackson.JsonMethods.compact(projectJValue) }

    println("** No type hints")
    new Bench()(DefaultFormats)
    println("** Short type hints")
    new Bench()(native.Serialization.formats(ShortTypeHints(classes)))
    println("** Full type hints")
    new Bench()(DefaultFormats + FullTypeHints(classes))
  }

  def benchmark(name: String)(f: => Any) = run(name, 120000, 20000)(f)

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
//
//    benchmark("Java serialization (deser)") { deserialize(ser1) }
//
    benchmark("json4s-native (deser)") { native.Serialization.read[Project](ser2) }
    benchmark("json4s-jackson (deser)") { jackson.Serialization.read[Project](ser2) }






  }

}

case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team]) extends Serializable
case class Language(name: String, version: Double) extends Serializable
case class Team(role: String, members: List[Employee]) extends Serializable
case class Employee(name: String, experience: Int) extends Serializable
