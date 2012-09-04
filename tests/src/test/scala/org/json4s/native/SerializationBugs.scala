package org.json4s

import org.specs2.mutable.Specification
import java.util.UUID
import org.json4s.native.Serialization

object SerializationBugs extends Specification {
  import Serialization.{read, write => swrite}

  implicit val formats = Serialization.formats(NoTypeHints)

  "plan1.Plan can be serialized (issue 341)" in {
    import plan1._

    val game = Game(Map("a" -> Plan(Some(Action(1, None)))))
    val ser = swrite(game)
    read[Game](ser) mustEqual game
  }

  "plan2.Plan can be serialized (issue 341)" in {
    import plan2._

    val g1 = Game(Map("a" -> Plan(Some(Action("f1", "s", Array(), None)),
                                  Some("A"),
                                  Some(Action("f2", "s2", Array(0, 1, 2), None)))))
    val ser = swrite(g1)
    val g2 = read[Game](ser)
    val plan = g2.buy("a")
    g2.buy.size mustEqual 1
    val leftOp = plan.leftOperand.get
    leftOp.functionName mustEqual "f1"
    leftOp.symbol mustEqual "s"
    leftOp.inParams.toList mustEqual Nil
    leftOp.subOperand mustEqual None
    plan.operator mustEqual Some("A")
    val rightOp = plan.rightOperand.get
    rightOp.functionName mustEqual "f2"
    rightOp.symbol mustEqual "s2"
    rightOp.inParams.toList mustEqual List(0, 1, 2)
    rightOp.subOperand mustEqual None
  }

  "null serialization bug" in {
    val x = new X(null)
    val ser = swrite(x)
    read[X](ser) mustEqual x
  }

  "StackOverflowError with large Lists" in {
    val xs = LongList(List.fill(5000)(0).map(Num))
    val ser = swrite(xs)
    read[LongList](ser).xs.length mustEqual 5000
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

    implicit val formats = Serialization.formats(NoTypeHints) + new UUIDFormat
    val o1 = OptionalUUID(None)
    val o2 = OptionalUUID(Some(UUID.randomUUID))
    read[OptionalUUID](swrite(o1)) mustEqual o1
    read[OptionalUUID](swrite(o2)) mustEqual o2
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
            .getOrElse(failure("No type parameter info for type Seq")).asInstanceOf[Class[_]], None)
          xs.map(x => Extraction.extract(x, typeInfo))
      }
    }

    implicit val formats = DefaultFormats + new SeqFormat

    val seq = Seq(1, 2, 3)
    val ser = Extraction.decompose(seq)
    Extraction.extract[Seq[Int]](ser) mustEqual seq
  }

  "Serialization of an opaque value should not fail" in {
    val o = Opaque(JObject(JField("some", JString("data")) :: Nil))
    val ser = Serialization.write(o)
    ser mustEqual """{"x":{"some":"data"}}"""
  }

  "Map with Map value" in {
    val a = Map("a" -> Map("a" -> 5))
    val b = Map("b" -> 1)
    val str = Serialization.write(MapWithMap(a, b))
    read[MapWithMap](str) mustEqual MapWithMap(a, b)
  }

  "Either can't be deserialized with type hints" in {
    implicit val formats = DefaultFormats + FullTypeHints(classOf[Either[_, _]] :: Nil)
    val x = Eith(Left("hello"))
    val s = Serialization.write(x)
    read[Eith](s) mustEqual x
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
        case SingleValue(x: Double) => JObject(List(JField("val", JDouble(x))))
        case VectorValue(x: Vector[Double]) => JObject(List(JField("val", JArray(x.toList.map(JDouble(_))))))
      }
    }

    implicit val formats = DefaultFormats + new SingleOrVectorSerializer

    val ser = swrite(MapHolder(Map("hello" -> SingleValue(2.0))))
    read[MapHolder](ser) mustEqual MapHolder(Map("hello" -> SingleValue(2.0)))
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
