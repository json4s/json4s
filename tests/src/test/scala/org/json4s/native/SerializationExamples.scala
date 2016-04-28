package org.json4s


import java.util.Date
import org.specs2.mutable.Specification

object SerializationExamples extends Specification {
  import native.Serialization.{read, write => swrite}

  implicit val formats = native.Serialization.formats(NoTypeHints)

  val project = Project("test", new Date, Some(Language("Scala", 2.75)), List(
    Team("QA", List(Employee("John Doe", 5), Employee("Mike", 3))),
    Team("Impl", List(Employee("Mark", 4), Employee("Mary", 5), Employee("Nick Noob", 1)))))

  "Project serialization example" in {
    val ser = swrite(project)
    read[Project](ser) must_== project
  }

  case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team])
  case class Language(name: String, version: Double)
  case class Team(role: String, members: List[Employee])
  case class Employee(name: String, experience: Int)

  "Null example" in {
    val ser = swrite(Nullable(null))
    read[Nullable](ser) must_== Nullable(null)
  }

  case class Nullable(name: String)

  "Lotto serialization example" in {
    import LottoExample.{Lotto, lotto}

    val ser = swrite(lotto)
    read[Lotto](ser) must_== lotto
  }

  "Primitive serialization example" in {
    val primitives = Primitives(124, 123L, 126.5, 127.5.floatValue, "128", 's, 125, 129.byteValue, true)
    val ser = swrite(primitives)
    read[Primitives](ser) must_== primitives
  }

  "Multidimensional list example" in {
    val ints = Ints(List(List(1, 2), List(3), List(4, 5)))
    val ser = swrite(ints)
    read[Ints](ser) must_== ints
  }

  "Map serialization example" in {
    val p = PersonWithAddresses("joe", Map("address1" -> Address("Bulevard", "Helsinki"),
                                           "address2" -> Address("Soho", "London")))
    val ser = swrite(p)
    read[PersonWithAddresses](ser) must_== p
  }

  "Recursive type serialization example" in {
    val r1 = Rec(1, Nil)
    val r2 = Rec(2, Nil)
    val r3 = Rec(3, r1 :: r2 :: Nil)

    val ser = swrite(r3)
    read[Rec](ser) must_== r3
  }

  "Set serialization example" in {
    val s = SetContainer(Set("foo", "bar"))
    val ser = swrite(s)
    read[SetContainer](ser) must_== s
  }

  "Array serialization example" in {
    val s = ArrayContainer(Array("foo", "bar"))
    val ser = swrite(s);
    val unser = read[ArrayContainer](ser)
    s.array.toList must_== unser.array.toList
  }

  "Seq serialization example" in {
    val s = SeqContainer(List("foo", "bar"))
    val ser = swrite(s)
    read[SeqContainer](ser) must_== s
  }

  "Option serialization example" in {
    val ser = swrite(Some(List(1, 2)))
    read[Option[List[Int]]](ser) must_== Some(List(1, 2))
    read[Option[List[Int]]]("") must_== None
  }

  "None Option of tuple serialization example" in {
    // This is a regression test case, failed in lift json
    val s = OptionOfTupleOfDouble(None)
    val ser = swrite(s)
    read[OptionOfTupleOfDouble](ser) must_== s
  }

  "Default parameter example" in {
    val pw = PlayerWithDefault("zortan")
    val ser = swrite(pw)
    ser must_== """{"name":"zortan","credits":5}"""
    read[PlayerWithDefault]("""{"name":"zortan"}""") must_== pw
  }

  "Default optional parameter example" in {
    val pw = PlayerWithOptionDefault("zoktan")
    val ser = swrite(pw)
    ser must_== """{"name":"zoktan","score":6}"""
    read[PlayerWithOptionDefault]("""{"name":"zoktan"}""") must_== pw
  }

  "Default recursive parameter example" in {
    val pw = PlayerWithGimmick("zaotan")
    val ser = swrite(pw)
    ser must_== """{"name":"zaotan","gimmick":{"name":"default"}}"""
    read[PlayerWithGimmick]("""{"name":"zaotan"}""") must_== pw
  }

  "Default for list argument example" in {
    val pw = PlayerWithList("oozton")
    val ser = swrite(pw)
    ser must_== """{"name":"oozton","badges":["intro","tutorial"]}"""
    read[PlayerWithList]("""{"name":"oozton"}""") must_== pw
  }

  "Case class with internal state example" in {
    val m = Members("s", 1)
    val ser = swrite(m)
    ser must_== """{"x":"s","y":1}"""
    read[Members](ser) must_== m
  }

  "Case class from type constructors example" in {
    val p = ProperType(TypeConstructor(Chicken(10)), (25, Player("joe")))
    val ser = swrite(p)
    read[ProperType](ser) must_== p
  }

  "Generic Map with simple values example" in {
    val pw = PlayerWithGenericMap("zortan", Map("1" -> "asd", "a" -> 3))
    val ser = swrite(pw)
    ser must_== """{"name":"zortan","infomap":{"1":"asd","a":3}}"""
    read[PlayerWithGenericMap](ser) must_== pw
  }

  "Generic Map with case class and type hint example" in {
    implicit val formats = native.Serialization.formats(ShortTypeHints(List(classOf[Player])))
    val pw = PlayerWithGenericMap("zortan", Map("1" -> "asd", "a" -> 3, "friend" -> Player("joe")))
    val ser = swrite(pw)
    ser must_== """{"name":"zortan","infomap":{"1":"asd","a":3,"friend":{"jsonClass":"Player","name":"joe"}}}"""
    read[PlayerWithGenericMap](ser) must_== pw
  }

  "Generic List with simple values example" in {
    val pw = PlayerWithGenericList("zortan", List("1", 3))
    val ser = swrite(pw)
    ser must_== """{"name":"zortan","infolist":["1",3]}"""
    read[PlayerWithGenericList](ser) must_== pw
  }

  "Generic List with objects and hints example" in {
    implicit val formats = native.Serialization.formats(ShortTypeHints(List(classOf[Player])))
    val pw = PlayerWithGenericList("zortan", List("1", 3, Player("joe")))
    val ser = swrite(pw)
    ser must_== """{"name":"zortan","infolist":["1",3,{"jsonClass":"Player","name":"joe"}]}"""
    read[PlayerWithGenericList](ser) must_== pw
  }

  // #246 Double.NaN serializes but does not deserialize
  "NaN Float serializes to null example" in {
    val expected = SingleValue(Float.NaN)
    val serialized = native.Serialization.write(expected)
    serialized must_== """{"value":null}"""
  }
  "NaN Double serializes to null example" in {
    val expected = SingleValue(Double.NaN)
    val serialized = native.Serialization.write(expected)
    serialized must_== """{"value":null}"""
  }
  "NaN String value won't be null" in {
    val expected = SingleValue("NaN")
    val serialized = native.Serialization.write(expected)
    serialized must_== """{"value":"NaN"}"""
  }

  case class Ints(x: List[List[Int]])

  case class Rec(n: Int, xs: List[Rec])

  case class Members(x: String, y: Int) {
    val foo1 = "foo"
    lazy val foo2 = "foo"
  }
}

object ShortTypeHintExamples extends TypeHintExamples {
  implicit val formats = native.Serialization.formats(ShortTypeHints(classOf[Fish] :: classOf[Dog] :: Nil))

  "Deserialization succeeds even if jsonClass is not the first field" in {
    val ser = """{"animals":[],"pet":{"name":"pluto","jsonClass":"Dog"}}"""
    native.Serialization.read[Animals](ser) must_== Animals(Nil, Dog("pluto"))
  }


}

object FullTypeHintExamples extends TypeHintExamples {
  import native.Serialization.{read, write => swrite}

  implicit val formats = native.Serialization.formats(FullTypeHints(List[Class[_]](classOf[Animal], classOf[True], classOf[False], classOf[Falcon], classOf[Chicken])))

  "Ambiguous field decomposition example" in {
    val a = Ambiguous(False())

    val ser = swrite(a)
    read[Ambiguous](ser) must_== a
  }

  "Ambiguous parameterized field decomposition example" in {
    val o = AmbiguousP(Chicken(23))

    val ser = swrite(o)
    read[AmbiguousP](ser) must_== o
  }

  "Option of ambiguous field decomposition example" in {
    val o = OptionOfAmbiguous(Some(True()))

    val ser = swrite(o)
    read[OptionOfAmbiguous](ser) must_== o
  }

  "Option of ambiguous parameterized field decomposition example" in {
    val o = OptionOfAmbiguousP(Some(Falcon(200.0)))

    val ser = swrite(o)
    read[OptionOfAmbiguousP](ser) must_== o
  }

  "Default recursive with type hints example" in {
    val pw = PlayerWithBird("zoltan")
    val ser = swrite(pw)
    ser must_== """{"name":"zoltan","bird":{"jsonClass":"org.json4s.Chicken","eggs":3}}"""
    read[PlayerWithBird]("""{"name":"zoltan"}""") must_== pw
  }
}

object CustomTypeHintFieldNameExample extends TypeHintExamples {
  import native.Serialization.{write => swrite}

  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[Fish] :: classOf[Dog] :: Nil)
    override val typeHintFieldName = "$type$"
  }

  "Serialized JSON contains configured field name" in {
    val animals = Animals(Dog("pluto") :: Fish(1.2) :: Nil, Dog("pluto"))
    val ser = swrite(animals)
    ser must_== """{"animals":[{"$type$":"Dog","name":"pluto"},{"$type$":"Fish","weight":1.2}],"pet":{"$type$":"Dog","name":"pluto"}}"""
  }
}

trait TypeHintExamples extends Specification {
  import native.Serialization.{read, write => swrite}

  implicit val formats: Formats

  "Polymorphic List serialization example" in {
    val animals = Animals(Dog("pluto") :: Fish(1.2) :: Dog("devil") :: Nil, Dog("pluto"))
    val ser = swrite(animals)
    read[Animals](ser) must_== animals
  }

  "Parameterized type serialization example" in {
    val objs = Objs(Obj(Fish(1.2)) :: Obj(Dog("pluto")) :: Nil)
    val ser = swrite(objs)
    read[Objs](ser) must_== objs
  }

  "Tuple serialization example" in {
    val t: (Animal, Animal) = (Fish(1.5), Dog("pluto"))
    val ser = swrite(t)
    read[(Animal, Animal)](ser) must_== t
  }
}

case class Animals(animals: List[Animal], pet: Animal)
trait Animal
case class Dog(name: String) extends Animal
case class Fish(weight: Double) extends Animal

case class Objs(objects: List[Obj[_]])
case class Obj[A](a: A)
object CustomSerializerExamples extends Specification {
  import native.Serialization.{read, write => swrite}
  import JsonAST._
  import java.util.regex.Pattern

  class IntervalSerializer extends CustomSerializer[Interval](format => (
    {
      case JObject(JField("start", JInt(s)) :: JField("end", JInt(e)) :: Nil) =>
        new Interval(s.longValue, e.longValue)
    },
    {
      case x: Interval =>
        JObject(JField("start", JInt(BigInt(x.startTime))) ::
                JField("end",   JInt(BigInt(x.endTime))) :: Nil)
    }
  ))

  class PatternSerializer extends CustomSerializer[Pattern](format => (
    {
      case JObject(JField("$pattern", JString(s)) :: Nil) => Pattern.compile(s)
    },
    {
      case x: Pattern => JObject(JField("$pattern", JString(x.pattern)) :: Nil)
    }
  ))

  class DateSerializer extends CustomSerializer[Date](format => (
    {
      case JObject(List(JField("$dt", JString(s)))) =>
        format.dateFormat.parse(s).getOrElse(throw new MappingException("Can't parse "+ s + " to Date"))
    },
    {
      case x: Date => JObject(JField("$dt", JString(format.dateFormat.format(x))) :: Nil)
    }
  ))

  class IndexedSeqSerializer extends Serializer[IndexedSeq[_]] {
    def deserialize(implicit format: Formats) = {
      case (TypeInfo(clazz, ptype), json) if classOf[IndexedSeq[_]].isAssignableFrom(clazz) => json match {
        case JArray(xs) =>
          val t = ptype.getOrElse(throw new MappingException("parameterized type not known"))
          xs.map(x => Extraction.extract(x, TypeInfo(t.getActualTypeArguments()(0).asInstanceOf[Class[_]], None))).toIndexedSeq
        case x => throw new MappingException(s"Can't convert $x to IndexedSeq")
      }
    }

    def serialize(implicit format: Formats) = {
      case i: IndexedSeq[_] => JArray(i.map(Extraction.decompose).toList)
    }
  }

  "Serialize with custom serializers" in {
    implicit val formats =  native.Serialization.formats(NoTypeHints) +
      new IntervalSerializer + new PatternSerializer + new DateSerializer + new IndexedSeqSerializer

    val i = new Interval(1, 4)
    val ser = swrite(i)
    ser mustEqual """{"start":1,"end":4}"""
    val i2 = read[Interval](ser)
    i2.startTime mustEqual i.startTime
    i2.endTime mustEqual i.endTime

    val pattern = Pattern.compile("^Curly")
    val pser = swrite(pattern)
    pser mustEqual """{"$pattern":"^Curly"}"""
    read[Pattern](pser).pattern mustEqual pattern.pattern

    val d = new Date(0)
    val dser = swrite(d)
    dser mustEqual """{"$dt":"1970-01-01T00:00:00.000Z"}"""
    read[Date](dser) mustEqual d

    val xs = Indexed(Vector("a", "b", "c"))
    val iser = swrite(xs)
    iser mustEqual """{"xs":["a","b","c"]}"""
    read[Indexed](iser).xs.toList mustEqual List("a","b","c")
  }


}

case class Indexed(xs: IndexedSeq[String])

class Interval(start: Long, end: Long) {
  val startTime = start
  val endTime = end
}

object CustomClassWithTypeHintsExamples extends Specification {
  import native.Serialization.{read, write => swrite}
  import JsonAST._

  val hints = new ShortTypeHints(classOf[DateTime] :: Nil) {
    override def serialize: PartialFunction[Any, JObject] = {
      case t: DateTime => JObject(JField("t", JInt(t.time)) :: Nil)
    }

    override def deserialize: PartialFunction[(String, JObject), Any] = {
      case ("DateTime", JObject(JField("t", JInt(t)) :: Nil)) => new DateTime(t.longValue)
    }
  }
  implicit val formats = native.Serialization.formats(hints)

  "Custom class serialization using provided serialization and deserialization functions" in {
    val m = Meeting("The place", new DateTime(1256681210802L))
    val ser = swrite(m)
    val m2 = read[Meeting](ser)
    m.place must_== m2.place
    m.time.time must_== m2.time.time
  }

  "List of custom classes example" in {
    val ts = Times(List(new DateTime(123L), new DateTime(234L)))
    val ser = swrite(ts)
    val ts2 = read[Times](ser)
    ts2.times(0).time must_== 123L
    ts2.times(1).time must_== 234L
    ts2.times.size must_== 2
  }

  "Custom serializer with default example" in {
    val m = MeetingWithDefault("The place")
    val ser = swrite(m)
    ser must_== """{"place":"The place","time":{"jsonClass":"DateTime","t":7777}}"""
    val m2 = read[MeetingWithDefault]("""{"place":"The place"}""")
    m.place must_== m2.place
    m.time.time must_== m2.time.time
  }

  "List of custom classes with default example" in {
    val ts = TimesWithDefault()
    val ser = swrite(ts)
    ser must_== """{"times":[{"jsonClass":"DateTime","t":8888}]}"""
    val ts2 = read[TimesWithDefault]("{}")
    ts2.times(0).time must_== 8888L
    ts2.times.size must_== 1
  }
}

case class Meeting(place: String, time: DateTime)
class DateTime(val time: Long)

case class Times(times: List[DateTime])

sealed abstract class Bool
case class True() extends Bool
case class False() extends Bool
case class Ambiguous(child: Bool)

trait Bird
case class Falcon(weight: Double) extends Bird
case class Chicken(eggs: Int) extends Bird

case class AmbiguousP(bird: Bird)

case class OptionOfAmbiguous(opt: Option[Bool])

case class OptionOfAmbiguousP(opt: Option[Bird])

case class SetContainer(set: Set[String])

case class ArrayContainer(array: Array[String])

case class SeqContainer(seq: Seq[String])

case class OptionOfTupleOfDouble(position: Option[Tuple2[Double, Double]])

case class Player(name: String)
case class TypeConstructor[A](x: A)
case class ProperType(x: TypeConstructor[Chicken], t: (Int, Player))

case class PlayerWithDefault(name: String, credits: Int = 5)
case class PlayerWithOptionDefault(name: String, score: Option[Int] = Some(6))
case class Gimmick(name: String)
case class PlayerWithGimmick(name: String, gimmick: Gimmick = Gimmick("default"))
case class PlayerWithBird(name: String, bird: Bird = Chicken(3))
case class PlayerWithList(name: String, badges: List[String] = List("intro", "tutorial"))
case class MeetingWithDefault(place: String, time: DateTime = new DateTime(7777L))
case class TimesWithDefault(times: List[DateTime] = List(new DateTime(8888L)))

case class PlayerWithGenericMap(name: String, infomap: Map[String, Any])
case class PlayerWithGenericList(name: String, infolist: List[Any])
