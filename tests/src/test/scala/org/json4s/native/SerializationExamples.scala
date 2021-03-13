package org.json4s

import java.util.Date
import org.scalatest.wordspec.AnyWordSpec

class SerializationExamples extends AnyWordSpec {

  import SerializationExamples._
  import native.Serialization.{read, write => swrite}

  implicit val formats: Formats = native.Serialization.formats(NoTypeHints)

  val project = Project(
    "test",
    new Date,
    Some(Language("Scala", 2.75)),
    List(
      Team("QA", List(Employee("John Doe", 5), Employee("Mike", 3))),
      Team("Impl", List(Employee("Mark", 4), Employee("Mary", 5), Employee("Nick Noob", 1)))
    )
  )

  "Project serialization example" in {
    val ser = swrite(project)
    assert(read[Project](ser) == project)
  }

  "Null example" in {
    val ser = swrite(Nullable(null))
    assert(read[Nullable](ser) == Nullable(null))
  }

  "symbol fields" in {
    val j = SymbolFields(42, "foo", true, 99, List(0.5))

    val ser = swrite(j)
    assert(read[SymbolFields](ser) == j)
  }

  "symbol two args lists" in {
    val symbolArgsTwoList = SymbolFieldsArgsTwoLists(1, 2)("Adam")
    val ser = swrite(symbolArgsTwoList)

    assert(ser == "{\"!@#\":1,\"-+>\":2}")
  }

  "Lotto serialization example" in {
    import LottoExample.{Lotto, lotto}

    val ser = swrite(lotto)
    assert(read[Lotto](ser) == lotto)
  }

  "Primitive serialization example" in {
    val primitives = Primitives(124, 123L, 126.5, 127.5.floatValue, "128", Symbol("s"), 125, 129.byteValue, true)
    val ser = swrite(primitives)
    assert(read[Primitives](ser) == primitives)
  }

  "Args two lists" in {
    val argsToLists = ArgsTwoLists("Adam", 23)("Ady")
    val ser = swrite(argsToLists)

    assert(ser == "{\"name\":\"Adam\",\"age\":23}")
  }

  "Multidimensional list example" in {
    val ints = Ints(List(List(1, 2), List(3), List(4, 5)))
    val ser = swrite(ints)
    assert(read[Ints](ser) == ints)
  }

  "Map serialization example" in {
    val p = PersonWithAddresses(
      "joe",
      Map("address1" -> Address("Bulevard", "Helsinki"), "address2" -> Address("Soho", "London"))
    )
    val ser = swrite(p)
    assert(read[PersonWithAddresses](ser) == p)
  }

  "Recursive type serialization example" in {
    val r1 = Rec(1, Nil)
    val r2 = Rec(2, Nil)
    val r3 = Rec(3, r1 :: r2 :: Nil)

    val ser = swrite(r3)
    assert(read[Rec](ser) == r3)
  }

  "Set serialization example" in {
    val s = SetContainer(Set("foo", "bar"))
    val ser = swrite(s)
    assert(read[SetContainer](ser) == s)
  }

  "Array serialization example" in {
    val s = ArrayContainer(Array("foo", "bar"))
    val ser = swrite(s)
    val unser = read[ArrayContainer](ser)
    assert(s.array.toList == unser.array.toList)
  }

  "Seq serialization example" in {
    val s = SeqContainer(List("foo", "bar"))
    val ser = swrite(s)
    assert(read[SeqContainer](ser) == s)
  }

  "Option serialization example" in {
    val ser = swrite(Some(List(1, 2)))
    assert(read[Option[List[Int]]](ser) == Some(List(1, 2)))
    assert(read[Option[List[Int]]]("") == None)
  }

  "None Option of tuple serialization example" in {
    // This is a regression test case, failed in lift json
    val s = OptionOfTupleOfDouble(None)
    val ser = swrite(s)
    assert(read[OptionOfTupleOfDouble](ser) == s)
  }

  "Default parameter example" in {
    val pw = PlayerWithDefault("zortan")
    val ser = swrite(pw)
    assert(ser == """{"name":"zortan","credits":5}""")
    assert(read[PlayerWithDefault]("""{"name":"zortan"}""") == pw)
  }

  "Default optional parameter example" in {
    val pw = PlayerWithOptionDefault("zoktan")
    val ser = swrite(pw)
    assert(ser == """{"name":"zoktan","score":6}""")
    assert(read[PlayerWithOptionDefault]("""{"name":"zoktan"}""") == pw)
  }

  "Default recursive parameter example" in {
    val pw = PlayerWithGimmick("zaotan")
    val ser = swrite(pw)
    assert(ser == """{"name":"zaotan","gimmick":{"name":"default"}}""")
    assert(read[PlayerWithGimmick]("""{"name":"zaotan"}""") == pw)
  }

  "Default for list argument example" in {
    val pw = PlayerWithList("oozton")
    val ser = swrite(pw)
    assert(ser == """{"name":"oozton","badges":["intro","tutorial"]}""")
    assert(read[PlayerWithList]("""{"name":"oozton"}""") == pw)
  }

  "Case class with internal state example" in {
    val m = Members("s", 1)
    val ser = swrite(m)
    assert(ser == """{"x":"s","y":1}""")
    assert(read[Members](ser) == m)
  }

  "Case class from type constructors example" in {
    val p = ProperType(TypeConstructor(Chicken(10)), (25, Player("joe")))
    val ser = swrite(p)
    assert(read[ProperType](ser) == p)
  }

  "Generic Map with simple values example" in {
    val pw = PlayerWithGenericMap("zortan", Map("1" -> "asd", "a" -> 3))
    val ser = swrite(pw)
    assert(ser == """{"name":"zortan","infomap":{"1":"asd","a":3}}""")
    assert(read[PlayerWithGenericMap](ser) == pw)
  }

  "Generic Map with case class and type hint example" in {
    implicit val formats: Formats = native.Serialization.formats(ShortTypeHints(List(classOf[Player])))
    val pw = PlayerWithGenericMap("zortan", Map("1" -> "asd", "a" -> 3, "friend" -> Player("joe")))
    val ser = swrite(pw)
    assert(ser == """{"name":"zortan","infomap":{"1":"asd","a":3,"friend":{"jsonClass":"Player","name":"joe"}}}""")
    assert(read[PlayerWithGenericMap](ser) == pw)
  }

  "Generic List with simple values example" in {
    val pw = PlayerWithGenericList("zortan", List("1", 3))
    val ser = swrite(pw)
    assert(ser == """{"name":"zortan","infolist":["1",3]}""")
    assert(read[PlayerWithGenericList](ser) == pw)
  }

  "Generic List with objects and hints example" in {
    implicit val formats: Formats = native.Serialization.formats(ShortTypeHints(List(classOf[Player])))
    val pw = PlayerWithGenericList("zortan", List("1", 3, Player("joe")))
    val ser = swrite(pw)
    assert(ser == """{"name":"zortan","infolist":["1",3,{"jsonClass":"Player","name":"joe"}]}""")
    assert(read[PlayerWithGenericList](ser) == pw)
  }

  // #246 Double.NaN serializes but does not deserialize
  "NaN Float serializes to null example" in {
    val expected = SingleValue(Float.NaN)
    val serialized = native.Serialization.write(expected)
    assert(serialized == """{"value":null}""")
  }
  "NaN Double serializes to null example" in {
    val expected = SingleValue(Double.NaN)
    val serialized = native.Serialization.write(expected)
    assert(serialized == """{"value":null}""")
  }
  "NaN String value won't be null" in {
    val expected = SingleValue("NaN")
    val serialized = native.Serialization.write(expected)
    assert(serialized == """{"value":"NaN"}""")
  }

  "Unknown type hint should not serialize" in {
    implicit val formats: Formats =
      native.Serialization.formats(ShortTypeHints(classOf[Iron] :: classOf[IronMaiden] :: Nil))
    val toSerialize = Materials(List(Oak(9)), Nil)
    val json = native.Serialization.write(toSerialize)

    assertThrows[MappingException] {
      read[Materials](json)
    }
  }

  "Multiple type hints should serialize" in {

    implicit val formats: Formats = native.Serialization.formats(
      ShortTypeHints(classOf[Oak] :: classOf[Cherry] :: Nil) +
      ShortTypeHints(classOf[Iron] :: classOf[IronMaiden] :: Nil)
    )
    val toSerialize = Materials(List(Oak(9)), Nil)
    val json = native.Serialization.write(toSerialize)

    assert(json == """{"woods":[{"jsonClass":"Oak","hardness":9}],"metals":[]}""")

  }
}

object SerializationExamples {

  case class Project(name: String, startDate: Date, lang: Option[Language], teams: List[Team])
  case class Language(name: String, version: Double)
  case class Team(role: String, members: List[Employee])
  case class Employee(name: String, experience: Int)

  case class ArgsTwoLists(name: String, age: Int)(nick: String) {
    val tmp = nick //nick becomes field of object
  }

  case class Nullable(name: String)

  case class SymbolFields(### : Int, !!! : String, +++ : Boolean, %%% : Long, @@@ : List[Double])

  case class SymbolFieldsArgsTwoLists(!@# : Int, -+> : Int)(%!@# : String) {
    val ### = %!@#
  }

  case class Ints(x: List[List[Int]])

  case class Rec(n: Int, xs: List[Rec])

  case class Members(x: String, y: Int) {
    val foo1 = "foo"
    lazy val foo2 = "foo"
  }
}

object ShortTypeHintExamples {
  val formats = native.Serialization.formats(ShortTypeHints(classOf[Fish] :: classOf[Dog] :: Nil))
}

class ShortTypeHintExamples extends TypeHintExamples {
  implicit val formats: Formats = ShortTypeHintExamples.formats

  "Deserialization succeeds even if jsonClass is not the first field" in {
    val ser = """{"animals":[],"pet":{"name":"pluto","jsonClass":"Dog"}}"""
    assert(native.Serialization.read[Animals](ser) == Animals(Nil, Dog("pluto")))
  }

  "Deserialization succeeds when a field name matching typeHintFieldName exists" in {
    val ser = """{"jsonClass":"Dog"}"""
    assert(native.Serialization.read[NotTypeHint](ser) == NotTypeHint("Dog"))
  }
}

class MappedHintExamples extends TypeHintExamples {
  implicit val formats: Formats =
    native.Serialization.formats(MappedTypeHints(Map(classOf[Fish] -> "fish", classOf[Dog] -> "dog")))

  "Serialization provides no type hint when not mapped" in {
    val animals = Animals(Dog("pluto") :: Fish(1.2) :: Turtle(103) :: Nil, Dog("pluto"))
    val ser = native.Serialization.write(animals)
    assert(
      ser == """{"animals":[{"jsonClass":"dog","name":"pluto"},{"jsonClass":"fish","weight":1.2},{"age":103}],"pet":{"jsonClass":"dog","name":"pluto"}}"""
    )
  }

  "Deserialization fails when type is not mapped" in {
    val ser = """{"animals":[],"pet":{"age":103,"jsonClass":"turtle"}}"""
    assertThrows[MappingException] { native.Serialization.read[Animals](ser) }
  }

  "Deserialization succeeds when a field name matching typeHintFieldName exists" in {
    val ser = """{"jsonClass":"turtle"}"""
    assert(native.Serialization.read[NotTypeHint](ser) == NotTypeHint("turtle"))
  }
}

object FullTypeHintExamples {
  val formats = native.Serialization.formats(
    FullTypeHints(List[Class[_]](classOf[Animal], classOf[True], classOf[False], classOf[Falcon], classOf[Chicken]))
  )
}
class FullTypeHintExamples extends TypeHintExamples {
  import native.Serialization.{read, write => swrite}

  implicit val formats: Formats = FullTypeHintExamples.formats

  "Ambiguous field decomposition example" in {
    val a = Ambiguous(False())

    val ser = swrite(a)
    assert(read[Ambiguous](ser) == a)
  }

  "Ambiguous parameterized field decomposition example" in {
    val o = AmbiguousP(Chicken(23))

    val ser = swrite(o)
    assert(read[AmbiguousP](ser) == o)
  }

  "Option of ambiguous field decomposition example" in {
    val o = OptionOfAmbiguous(Some(True()))

    val ser = swrite(o)
    assert(read[OptionOfAmbiguous](ser) == o)
  }

  "Option of ambiguous parameterized field decomposition example" in {
    val o = OptionOfAmbiguousP(Some(Falcon(200.0)))

    val ser = swrite(o)
    assert(read[OptionOfAmbiguousP](ser) == o)
  }

  "Default recursive with type hints example" in {
    val pw = PlayerWithBird("zoltan")
    val ser = swrite(pw)
    assert(ser == """{"name":"zoltan","bird":{"jsonClass":"org.json4s.Chicken","eggs":3}}""")
    assert(read[PlayerWithBird]("""{"name":"zoltan"}""") == pw)
  }

  "Deserialization succeeds when a field name matching typeHintFieldName exists" in {
    val ser = """{"jsonClass":"org.json4s.Chicken"}"""
    assert(read[NotTypeHint](ser) == NotTypeHint("org.json4s.Chicken"))
  }
}

class CustomTypeHintFieldNameExample extends TypeHintExamples {
  import native.Serialization.{write => swrite}

  implicit val formats: Formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[Fish] :: classOf[Dog] :: Nil, "$type$")
  }

  "Serialized JSON contains configured field name" in {
    val animals = Animals(Dog("pluto") :: Fish(1.2) :: Nil, Dog("pluto"))
    val ser = swrite(animals)
    assert(
      ser == """{"animals":[{"$type$":"Dog","name":"pluto"},{"$type$":"Fish","weight":1.2}],"pet":{"$type$":"Dog","name":"pluto"}}"""
    )
  }
}

trait TypeHintExamples extends AnyWordSpec {
  import native.Serialization.{read, write => swrite}

  implicit val formats: Formats

  "Polymorphic List serialization example" in {
    val animals = Animals(Dog("pluto") :: Fish(1.2) :: Dog("devil") :: Nil, Dog("pluto"))
    val ser = swrite(animals)
    assert(read[Animals](ser) == animals)
  }

  "Parameterized type serialization example" in {
    val objs = Objs(Obj(Fish(1.2)) :: Obj(Dog("pluto")) :: Nil)
    val ser = swrite(objs)
    assert(read[Objs](ser) == objs)
  }

  "Tuple serialization example" in {
    val t: (Animal, Animal) = (Fish(1.5), Dog("pluto"))
    val ser = swrite(t)
    assert(read[(Animal, Animal)](ser) == t)
  }
}

case class NotTypeHint(jsonClass: String)

case class Animals(animals: List[Animal], pet: Animal)
trait Animal
case class Dog(name: String) extends Animal
case class Fish(weight: Double) extends Animal
case class Turtle(age: Int) extends Animal

case class Objs(objects: List[Obj[_]])
case class Obj[A](a: A)
class CustomSerializerExamples extends AnyWordSpec {
  import native.Serialization.{read, write => swrite}
  import JsonAST._
  import java.util.regex.Pattern

  class IntervalSerializer
    extends CustomSerializer[Interval](format =>
      (
        { case JObject(JField("start", JInt(s)) :: JField("end", JInt(e)) :: Nil) =>
          new Interval(s.longValue, e.longValue)
        },
        { case x: Interval =>
          JObject(
            JField("start", JInt(BigInt(x.startTime))) ::
            JField("end", JInt(BigInt(x.endTime))) :: Nil
          )
        }
      )
    )

  class PatternSerializer
    extends CustomSerializer[Pattern](format =>
      (
        { case JObject(JField("$pattern", JString(s)) :: Nil) =>
          Pattern.compile(s)
        },
        { case x: Pattern =>
          JObject(JField("$pattern", JString(x.pattern)) :: Nil)
        }
      )
    )

  class DateSerializer
    extends CustomSerializer[Date](format =>
      (
        { case JObject(List(JField("$dt", JString(s)))) =>
          format.dateFormat.parse(s).getOrElse(throw new MappingException("Can't parse " + s + " to Date"))
        },
        { case x: Date =>
          JObject(JField("$dt", JString(format.dateFormat.format(x))) :: Nil)
        }
      )
    )

  class IndexedSeqSerializer extends Serializer[IndexedSeq[_]] {
    def deserialize(implicit format: Formats) = {
      case (TypeInfo(clazz, ptype), json) if classOf[IndexedSeq[_]].isAssignableFrom(clazz) =>
        json match {
          case JArray(xs) =>
            val t = ptype.getOrElse(throw new MappingException("parameterized type not known"))
            xs.map(x => Extraction.extract(x, TypeInfo(t.getActualTypeArguments()(0).asInstanceOf[Class[_]], None)))
              .toIndexedSeq
          case x => throw new MappingException(s"Can't convert $x to IndexedSeq")
        }
    }

    def serialize(implicit format: Formats) = { case i: IndexedSeq[_] =>
      JArray(i.map(Extraction.decompose).toList)
    }
  }

  "Serialize with custom serializers" in {
    implicit val formats: Formats = native.Serialization.formats(NoTypeHints) +
      new IntervalSerializer + new PatternSerializer + new DateSerializer + new IndexedSeqSerializer

    val i = new Interval(1, 4)
    val ser = swrite(i)
    assert(ser == """{"start":1,"end":4}""")
    val i2 = read[Interval](ser)
    assert(i2.startTime == i.startTime)
    assert(i2.endTime == i.endTime)

    val pattern = Pattern.compile("^Curly")
    val pser = swrite(pattern)
    assert(pser == """{"$pattern":"^Curly"}""")
    assert(read[Pattern](pser).pattern == pattern.pattern)

    val d = new Date(0)
    val dser = swrite(d)
    assert(dser == """{"$dt":"1970-01-01T00:00:00.000Z"}""")
    assert(read[Date](dser) == d)

    val xs = Indexed(Vector("a", "b", "c"))
    val iser = swrite(xs)
    assert(iser == """{"xs":["a","b","c"]}""")
    assert(read[Indexed](iser).xs.toList == List("a", "b", "c"))
  }

}

case class Indexed(xs: IndexedSeq[String])

class Interval(start: Long, end: Long) {
  val startTime = start
  val endTime = end
}

class CustomClassWithTypeHintsExamples extends AnyWordSpec {
  import native.Serialization.{read, write => swrite}
  import JsonAST._

  val hints = new ShortTypeHints(classOf[DateTime] :: Nil) {
    override def serialize: PartialFunction[Any, JObject] = { case t: DateTime =>
      JObject(JField("t", JInt(t.time)) :: Nil)
    }

    override def deserialize: PartialFunction[(String, JObject), Any] = {
      case ("DateTime", JObject(JField("t", JInt(t)) :: Nil)) => new DateTime(t.longValue)
    }
  }
  implicit val formats: Formats = native.Serialization.formats(hints)

  "Custom class serialization using provided serialization and deserialization functions" in {
    val m = Meeting("The place", new DateTime(1256681210802L))
    val ser = swrite(m)
    val m2 = read[Meeting](ser)
    assert(m.place == m2.place)
    assert(m.time.time == m2.time.time)
  }

  "List of custom classes example" in {
    val ts = Times(List(new DateTime(123L), new DateTime(234L)))
    val ser = swrite(ts)
    val ts2 = read[Times](ser)
    assert(ts2.times(0).time == 123L)
    assert(ts2.times(1).time == 234L)
    assert(ts2.times.size == 2)
  }

  "Custom serializer with default example" in {
    val m = MeetingWithDefault("The place")
    val ser = swrite(m)
    assert(ser == """{"place":"The place","time":{"jsonClass":"DateTime","t":7777}}""")
    val m2 = read[MeetingWithDefault]("""{"place":"The place"}""")
    assert(m.place == m2.place)
    assert(m.time.time == m2.time.time)
  }

  "List of custom classes with default example" in {
    val ts = TimesWithDefault()
    val ser = swrite(ts)
    assert(ser == """{"times":[{"jsonClass":"DateTime","t":8888}]}""")
    val ts2 = read[TimesWithDefault]("{}")
    assert(ts2.times(0).time == 8888L)
    assert(ts2.times.size == 1)
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
