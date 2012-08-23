import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import java.io._
import java.util.Date

object Serbench extends Benchmark {
  val classes = List(classOf[Project], classOf[Team], classOf[Employee], classOf[Language])
  val project = Project("test", new Date, Some(Language("Scala", 2.75)), List(
    Team("QA", List(Employee("John Doe", 5), Employee("Mike", 3))),
    Team("Impl", List(Employee("Mark", 4), Employee("Mary", 5), Employee("Nick Noob", 1)))))

  def main(args: Array[String]) = {
    println("** No type hints")
    new Bench()(Serialization.formats(NoTypeHints))
    println("** Short type hints")
    new Bench()(Serialization.formats(ShortTypeHints(classes)))
    println("** Full type hints")
    new Bench()(Serialization.formats(FullTypeHints(classes)))
  }

  class Bench(implicit formats: Formats) {
    benchmark("Java serialization (full)") { deserialize(serialize(project)) }
    benchmark("lift-json (full)") { read[Project](write(project)) }
    benchmark("Java serialization (ser)") { serialize(project) }
    benchmark("lift-json (ser)") { write(project) }
    val ser1 = serialize(project)
    val ser2 = write(project)
    benchmark("Java serialization (deser)") { deserialize(ser1) }
    benchmark("lift-json (deser)") { read[Project](ser2) }

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

  case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team]) extends Serializable
  case class Language(name: String, version: Double) extends Serializable
  case class Team(role: String, members: List[Employee]) extends Serializable
  case class Employee(name: String, experience: Int) extends Serializable
}
