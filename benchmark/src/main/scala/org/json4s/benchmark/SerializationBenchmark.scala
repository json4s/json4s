package org.json4s
package benchmark

import java.util.Date
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.util.concurrent.atomic.AtomicInteger

class SerializationNoTypeHintsBenchmark extends SimpleScalaBenchmark {

  private[this] val counter = new AtomicInteger(0)
  private[this] var project: Project = _
  private implicit val formats: Formats = DefaultFormats
  private[this] val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)


  override def setUp() {
    val c = counter.incrementAndGet()
    project = Project("test"+c, new Date, Some(Language("Scala"+c, 2.75+c)), List(
          Team("QA"+c, List(Employee("John Doe"+c, 5+c), Employee("Mike"+c, 3+c))),
          Team("Impl"+c, List(Employee("Mark"+c, 4+c), Employee("Mary"+c, 5+c), Employee("Nick Noob"+c, 1+c)))))
  }

  def timeJacksonSerialization(reps: Int) = repeat(reps) { mapper.writeValueAsString(project) }

  def timeJson4sNativeSerialization(reps: Int) = repeat(reps) {  native.Serialization.write(project) }

  def timeJson4sJacksonSerialization(reps: Int) = repeat(reps) { jackson.Serialization.write(project) }
}