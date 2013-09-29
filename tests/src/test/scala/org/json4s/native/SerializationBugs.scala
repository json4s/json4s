package org.json4s

import org.specs2.mutable.Specification
import java.util.UUID

object SerializationBugs extends Specification {
  import native.Serialization.{ read, write => swrite }

  implicit val formats = native.Serialization.formats(NoTypeHints)

  "plan1.Plan can be serialized (issue 341)" in {
    import plan1._

    val game = Game(Map("a" -> Plan(Some(Action(1, None)))))
    val ser = swrite(game)
    read[Game](ser) must_== game
  }

  "plan2.Plan can be serialized (issue 341)" in {
    import plan2._

    val g1 = Game(Map("a" -> Plan(Some(Action("f1", "s", Array(), None)),
      Some("A"),
      Some(Action("f2", "s2", Array[Number](0, 1, 2), None)))))
    val ser = swrite(g1)
    val g2 = read[Game](ser)
    val plan = g2.buy("a")
    g2.buy.size must_== 1
    val leftOp = plan.leftOperand.get
    leftOp.functionName must_== "f1"
    leftOp.symbol must_== "s"
    leftOp.inParams.toList must_== Nil
    leftOp.subOperand must_== None
    plan.operator must_== Some("A")
    val rightOp = plan.rightOperand.get
    rightOp.functionName must_== "f2"
    rightOp.symbol must_== "s2"
    rightOp.inParams.toList must_== List(0, 1, 2)
    rightOp.subOperand must_== None
  }

  "null serialization bug" in {
    val x = new X(null)
    val ser = swrite(x)
    read[X](ser) must_== x
  }

  "StackOverflowError with large Lists" in {
    val xs = LongList(List.fill(5000)(0).map(Num))
    val ser = swrite(xs)
    read[LongList](ser).xs.length must_== 5000
  }

  "Custom serializer should work with Option" in {
    class UUIDFormat extends Serializer[UUID] {
      val UUIDClass = classOf[UUID]

      def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
        case (TypeInfo(UUIDClass, _), JString(x)) => UUID.fromString(x)
      }

      def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
        case x: UUID => JString(x.toString)
      }
    }

    implicit val formats = native.Serialization.formats(NoTypeHints) + new UUIDFormat
    val o1 = OptionalUUID(None)
    val o2 = OptionalUUID(Some(UUID.randomUUID))
    read[OptionalUUID](swrite(o1)) must_== o1
    read[OptionalUUID](swrite(o2)) must_== o2
  }

  "TypeInfo is not correctly constructed for customer serializer -- 970" in {
    class SeqFormat extends Serializer[Seq[_]] {
      val SeqClass = classOf[Seq[_]]

      def serialize(implicit format: Formats) = {
        case seq: Seq[_] => JArray(seq.toList.map(Extraction.decompose))
      }

      def deserialize(implicit format: Formats) = {
        case (TypeInfo(SeqClass, parameterizedType), JArray(xs)) =>
          val typeInfo = TypeInfo(parameterizedType
            .map(_.getActualTypeArguments()(0))
            .getOrElse(reflect.fail("No type parameter info for type Seq")).asInstanceOf[Class[_]], None)
          xs.map(x => Extraction.extract(x, typeInfo))
      }
    }

    implicit val formats = DefaultFormats + new SeqFormat

    val seq = Seq(1, 2, 3)
    val ser = Extraction.decompose(seq)
    Extraction.extract[Seq[Int]](ser) must_== seq
  }

  "Serialization of an opaque value should not fail" in {
    val o = Opaque(JObject(JField("some", JString("data")) :: Nil))
    val ser = native.Serialization.write(o)
    ser must_== """{"x":{"some":"data"}}"""
  }

  "Map with Map value" in {
    val a = Map("a" -> Map("a" -> 5))
    val b = Map("b" -> 1)
    val str = native.Serialization.write(MapWithMap(a, b))
    read[MapWithMap](str) must_== MapWithMap(a, b)
  }

  "Either can't be deserialized with type hints" in {
    implicit val formats = DefaultFormats + FullTypeHints(classOf[Either[_, _]] :: Nil)
    val x = Eith(Left("hello"))
    val s = native.Serialization.write(x)
    read[Eith](s) must_== x
  }

  "Custom serializer should work as Map key (scala 2.9) (issue #1077)" in {
    class SingleOrVectorSerializer extends Serializer[SingleOrVector[Double]] {
      private val singleOrVectorClass = classOf[SingleOrVector[Double]]

      def deserialize(implicit format: Formats) = {
        case (TypeInfo(`singleOrVectorClass`, _), json) => json match {
          case JObject(List(JField("val", JDouble(x)))) => SingleValue(x)
          case JObject(List(JField("val", JArray(xs: List[JDouble])))) => VectorValue(xs.map(_.num).toIndexedSeq)
          case x => throw new MappingException("Can't convert " + x + " to SingleOrVector")
        }
      }

      def serialize(implicit format: Formats) = {
        case SingleValue(x: Double)         => JObject(List(JField("val", JDouble(x))))
        case VectorValue(x: Vector[Double]) => JObject(List(JField("val", JArray(x.toList.map(JDouble(_))))))
      }
    }

    implicit val formats = DefaultFormats + new SingleOrVectorSerializer

    val ser = swrite(MapHolder(Map("hello" -> SingleValue(2.0))))
    read[MapHolder](ser) must_== MapHolder(Map("hello" -> SingleValue(2.0)))
  }

  "Serialization of case class with many Option[T] fields" should {

    "produce valid JSON object with all fields undefined (set to None)" in {
      val optFields = OptionalFields(None, None, None, None)
      val str = native.Serialization.write(optFields)
      str must_== "{}"
    }

    "produce valid JSON object with one string field defined" in {
      val optFields = OptionalFields(Some("hello"), None, None, None)
      val str = native.Serialization.write(optFields)
      str must_== """{"optString":"hello"}"""
    }

    "produce valid JSON object with two fields defined #1" in {
      val optFields = OptionalFields(Some("hello"), None, None, Some(OptionalFields(None, None, None, None)))
      val str = native.Serialization.write(optFields)
      str must_== """{"optString":"hello","optObj":{}}"""
    }

    "produce valid JSON object with two fields defined #2" in {
      val optFields = OptionalFields(None, None, Some(1.0), Some(OptionalFields(None, None, None, None)))
      val str = native.Serialization.write(optFields)
      str must_== """{"optDouble":1.0,"optObj":{}}"""
    }

    "produce valid JSON object with all fields defined" in {
      val optFields = OptionalFields(Some("hello"), Some(42), Some(1.0), Some(OptionalFields(None, None, None, None)))
      val str = native.Serialization.write(optFields)
      str must_== """{"optString":"hello","optInt":42,"optDouble":1.0,"optObj":{}}"""
    }

    "produce valid JSON object with nested JSON object" in {
      val optFields = OptionalFields(None, None, None,
        Some(OptionalFields(None, None, None, None)))
      val str = native.Serialization.write(optFields)
      str must_== """{"optObj":{}}"""
    }

    "produce valid JSON object with deeply nested JSON objects" in {
      val optFields = OptionalFields(None, None, None,
        Some(OptionalFields(None, None, None,
          Some(OptionalFields(None, None, None,
            Some(OptionalFields(None, None, None,
              Some(OptionalFields(None, None, None, None)))))))))
      val str = native.Serialization.write(optFields)
      str must_== """{"optObj":{"optObj":{"optObj":{"optObj":{}}}}}"""
    }
  }

  "Escapes control characters" in {
    val ser = native.Serialization.write("\u0000\u001f")
    ser must_== "\"\\u0000\\u001f\""
  }

  "classes in deeply nested objects can be serialized" in {
    val ser = swrite(Zot.Bar.Foo("s"))
    read[Zot.Bar.Foo](ser).s must_== "s"
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
  case class Plan(leftOperand: Option[Action], operator: Option[String],
                  rightOperand: Option[Action])
  case class Game(buy: Map[String, Plan])
  case class Action(functionName: String, symbol: String,
                    inParams: Array[Number], subOperand: Option[Action])
}

case class Opaque(x: JValue)

sealed trait SingleOrVector[A]
case class SingleValue[A](value: A) extends SingleOrVector[A]
case class VectorValue[A](value: IndexedSeq[A]) extends SingleOrVector[A]

case class MapHolder(a: Map[String, SingleOrVector[Double]])

case class OptionalFields(optString: Option[String], optInt: Option[Int], optDouble: Option[Double], optObj: Option[OptionalFields])

object Zot {
  object Bar {
    case class Foo(s: String)
  }
}

