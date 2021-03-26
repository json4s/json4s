package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import java.util.UUID
import scala.collection.mutable

class SerializationBugs extends AnyWordSpec {
  import native.Serialization.{read, write => swrite}

  implicit val formats: Formats = native.Serialization.formats(NoTypeHints)

  "plan1.Plan can be serialized (issue 341)" in {
    import plan1._

    val game = Game(Map("a" -> Plan(Some(Action(1, None)))))
    val ser = swrite(game)
    assert(read[Game](ser) == game)
  }

  "plan2.Plan can be serialized (issue 341)" in {
    import plan2._

    val g1 = Game(
      Map(
        "a" -> Plan(
          Some(Action("f1", "s", Array(), None)),
          Some("A"),
          Some(Action("f2", "s2", Array[Number](0, 1, 2), None))
        )
      )
    )
    val ser = swrite(g1)
    val g2 = read[Game](ser)
    val plan = g2.buy("a")
    assert(g2.buy.size == 1)
    val leftOp = plan.leftOperand.get
    assert(leftOp.functionName == "f1")
    assert(leftOp.symbol == "s")
    assert(leftOp.inParams.toList == Nil)
    assert(leftOp.subOperand == None)
    assert(plan.operator == Some("A"))
    val rightOp = plan.rightOperand.get
    assert(rightOp.functionName == "f2")
    assert(rightOp.symbol == "s2")
    assert(rightOp.inParams.toList == List(0, 1, 2))
    assert(rightOp.subOperand == None)
  }

  "null serialization bug" in {
    val x = new X(null)
    val ser = swrite(x)
    assert(read[X](ser) == x)
  }

  "StackOverflowError with large Lists" in {
    val xs = LongList(List.fill(5000)(0).map(Num))
    val ser = swrite(xs)
    assert(read[LongList](ser).xs.length == 5000)
  }

  "Custom serializer should work with Option" in {
    class UUIDFormat extends Serializer[UUID] {
      val UUIDClass = classOf[UUID]

      def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
        case (TypeInfo(UUIDClass, _), JString(x)) => UUID.fromString(x)
      }

      def serialize(implicit format: Formats): PartialFunction[Any, JValue] = { case x: UUID =>
        JString(x.toString)
      }
    }

    implicit val formats: Formats = native.Serialization.formats(NoTypeHints) + new UUIDFormat
    val o1 = OptionalUUID(None)
    val o2 = OptionalUUID(Some(UUID.randomUUID))
    assert(read[OptionalUUID](swrite(o1)) == o1)
    assert(read[OptionalUUID](swrite(o2)) == o2)
  }

  "TypeInfo is not correctly constructed for customer serializer -- 970" in {
    class SeqFormat extends Serializer[Seq[_]] {
      val SeqClass = classOf[Seq[_]]

      def serialize(implicit format: Formats) = { case seq: Seq[_] =>
        JArray(seq.toList.map(Extraction.decompose(_)(format)))
      }

      def deserialize(implicit format: Formats) = { case (TypeInfo(SeqClass, parameterizedType), JArray(xs)) =>
        val typeInfo = TypeInfo(
          parameterizedType
            .map(_.getActualTypeArguments()(0))
            .getOrElse(reflect.fail("No type parameter info for type Seq"))
            .asInstanceOf[Class[_]],
          None
        )
        xs.map(x => Extraction.extract(x, typeInfo)(format))
      }
    }

    implicit val formats: Formats = DefaultFormats + new SeqFormat

    val seq = Seq(1, 2, 3)
    val ser = Extraction.decompose(seq)
    assert(Extraction.extract[Seq[Int]](ser) == seq)
  }

  "Serialization of an opaque value should not fail" in {
    val o = Opaque(JObject(JField("some", JString("data")) :: Nil))
    val ser = native.Serialization.write(o)
    assert(ser == """{"x":{"some":"data"}}""")
  }

  "Map with Map value" in {
    val a = Map("a" -> Map("a" -> 5))
    val b = Map("b" -> 1)
    val str = native.Serialization.write(MapWithMap(a, b))
    assert(read[MapWithMap](str) == MapWithMap(a, b))
  }

  "Either can't be deserialized with type hints" in {
    implicit val formats: Formats = DefaultFormats + FullTypeHints(classOf[Either[_, _]] :: Nil)
    val x = Eith(Left("hello"))
    val s = native.Serialization.write(x)
    assert(read[Eith](s) == x)
  }

  "Deserialization of nested non-terminal types w/o type information should not suppress type hinted deserialization" in {
    implicit val formats: Formats = DefaultFormats + ShortTypeHints(List(classOf[Y]))

    def test[Expected <: AnyRef, Actual](expected: Expected)(implicit mf: Manifest[Actual]) = {
      val json = native.Serialization.write[Expected](expected)
      val actual = read[Actual](json)

      assert(actual == expected)
    }

    test[Seq[Seq[Any]], Seq[_]](Seq(Seq[Any](1, Y("foo"), "bar")))
    test[Seq[Map[String, Y]], Seq[_]](Seq(Map("f1" -> Y("foo"))))
    test[Map[String, Seq[Y]], Map[String, _]](Map("f1" -> Seq(Y("foo"))))
    test[Map[String, Map[String, Y]], Map[String, _]](Map("f1" -> Map("f2" -> Y("foo"))))

    // and then really run it through the ringer

    test[Seq[Seq[Map[String, Any]]], Seq[_]](
      Seq(Seq(Map("f1" -> Map("f2" -> Seq(Seq[Any](1, Y("foo"), "bar"))), "f1" -> 2.0)))
    )
  }

  "Custom serializer should work as Map key (scala 2.9) (issue #1077)" in {
    class SingleOrVectorSerializer extends Serializer[SingleOrVector[Double]] {
      private[this] val singleOrVectorClass = classOf[SingleOrVector[Double]]

      def deserialize(implicit format: Formats) = { case (TypeInfo(`singleOrVectorClass`, _), json) =>
        json match {
          case JObject(List(JField("val", JDouble(x)))) => SingleValue(x)
          case JObject(List(JField("val", JArray(xs: List[_])))) =>
            VectorValue(xs.map(_.asInstanceOf[JDouble].num).toIndexedSeq)
          case x => throw new MappingException(s"Can't convert $x to SingleOrVector")
        }
      }

      def serialize(implicit format: Formats) = {
        case SingleValue(x: Double) => JObject(List(JField("val", JDouble(x))))
        case VectorValue(x: Vector[_]) =>
          JObject(List(JField("val", JArray(x.map(_.asInstanceOf[Double]).toList.map(JDouble(_))))))
      }
    }

    implicit val formats: Formats = DefaultFormats + new SingleOrVectorSerializer

    val ser = swrite(MapHolder(Map("hello" -> SingleValue(2.0))))
    assert(read[MapHolder](ser) == MapHolder(Map("hello" -> SingleValue(2.0))))
  }

  "Escapes control characters" in {
    val ser = native.Serialization.write("\u0000\u001F")
    assert(ser == "\"\\u0000\\u001F\"")
  }

  "Escapes control and unicode characters" in {
    val formats = DefaultFormats.withEscapeUnicode
    val ser = native.Serialization.write("\u0000\u001F")(formats)
    assert(ser == "\"\u0000\u001F\"")
  }

  "classes in deeply nested objects can be serialized" in {
    val ser = swrite(Zot.Bar.Foo("s"))
    assert(read[Zot.Bar.Foo](ser).s == "s")
  }

  "mutable Map can be serialized" in {
    val ser = swrite(mutable.Map("f" -> 1))
    assert(ser == """{"f":1}""")
  }

  "PositiveInfinity Float can be serialized" in {
    val expected = SingleValue(Float.PositiveInfinity)
    val serialized = native.Serialization.write(expected)
    val deserialized = read[SingleValue[Float]](serialized)
    assert(expected.value == deserialized.value)
  }

  "NegativeInfinity Float can be serialized" in {
    val expected = SingleValue(Float.NegativeInfinity)
    val serialized = native.Serialization.write(expected)
    val deserialized = read[SingleValue[Float]](serialized)
    assert(expected.value == deserialized.value)
  }

  "PositiveInfinity Double can be serialized" in {
    val expected = SingleValue(Double.PositiveInfinity)
    val serialized = native.Serialization.write(expected)
    val deserialized = read[SingleValue[Double]](serialized)
    assert(expected.value == deserialized.value)
  }

  "NegativeInfinity Double can be serialized" in {
    val expected = SingleValue(Double.NegativeInfinity)
    val serialized = native.Serialization.write(expected)
    val deserialized = read[SingleValue[Double]](serialized)
    assert(expected.value == deserialized.value)
  }
}

case class Eith(x: Either[String, Int])

case class MapWithMap(a: Map[String, Map[String, Int]], b: Map[String, Int])

case class LongList(xs: List[Num])
case class Num(x: Int)

case class X(yy: Y)
case class Y(ss: String)

case class OptionalUUID(uuid: Option[UUID])

package plan1 {
  case class Plan(plan: Option[Action])
  case class Game(game: Map[String, Plan])
  case class Action(id: Int, subAction: Option[Action])
}

package plan2 {
  case class Plan(leftOperand: Option[Action], operator: Option[String], rightOperand: Option[Action])
  case class Game(buy: Map[String, Plan])
  case class Action(functionName: String, symbol: String, inParams: Array[Number], subOperand: Option[Action])
}

case class Opaque(x: JValue)

sealed trait SingleOrVector[A]
case class SingleValue[A](value: A) extends SingleOrVector[A]
case class VectorValue[A](value: IndexedSeq[A]) extends SingleOrVector[A]

case class MapHolder(a: Map[String, SingleOrVector[Double]])

object Zot {
  object Bar {
    case class Foo(s: String)
  }
}
