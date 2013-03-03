package org.json4s

import org.specs.Specification
import java.util.Date
import reflect._

case class RRSimple(id: Int, name: String, items: List[String], createdAt: Date)
case class RRSimpleJoda(id: Int, name: String, items: List[String], createdAt: DateTime)
case class RROption(id: Int, name: String, status: Option[String], code: Option[Int], createdAt: Date, deletedAt: Option[Date])
case class RRTypeParam[T](id: Int, name: String, value: T, opt: Option[T], seq: Seq[T], map: Map[String, T])
//case class RRTypeParam[T](opt: Option[T])
case class Response(data: List[Map[String, Int]])
case class NestedType(dat: List[Map[Double, Option[Int]]], lis: List[List[List[List[List[Int]]]]])
case class NestedResType[T, S, V <: Option[S]](t: T, v: V, dat: List[Map[T, V]], lis: List[List[List[List[List[S]]]]])

class ReflectorSpec extends Specification {
  "Reflector" should {
    "describe a simple case class" in {
      val desc = Reflector.describe[RRSimple].asInstanceOf[ClassDescriptor]
      desc.constructors.size must_== 1
      val params = desc.constructors.head.params
      params(0).name must_== "id"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[Int]
      params(1).name must_== "name"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[String]
      params(2).name must_== "items"
      params(2).defaultValue must beNone
      params(2).argType must_== Reflector.scalaTypeOf[List[String]]
      params(3).name must_== "createdAt"
      params(3).defaultValue must beNone
      params(3).argType must_== Reflector.scalaTypeOf[Date]
    }
    "describe a simple joda case class" in {
      val desc = Reflector.describe[RRSimpleJoda].asInstanceOf[ClassDescriptor]
      desc.constructors.size must_== 1
      val params = desc.constructors.head.params
      params(0).name must_== "id"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[Int]
      params(1).name must_== "name"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[String]
      params(2).name must_== "items"
      params(2).defaultValue must beNone
      params(2).argType must_== Reflector.scalaTypeOf[List[String]]
      params(3).name must_== "createdAt"
      params(3).defaultValue must beNone
      params(3).argType must_== Reflector.scalaTypeOf[DateTime]
    }
    "Describe a case class with options" in {
      val desc = Reflector.describe[RROption].asInstanceOf[ClassDescriptor]
      desc.constructors.size must_== 1
      val params = desc.constructors.head.params
      params(0).name must_== "id"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[Int]
      params(1).name must_== "name"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[String]
      params(2).name must_== "status"
      params(2).defaultValue must beNone
      params(2).argType must_== Reflector.scalaTypeOf[Option[String]]
      params(2).argType.typeArgs must_== Seq(Reflector.scalaTypeOf[String])
      params(3).name must_== "code"
      params(3).defaultValue must beNone
      params(3).argType must_== Reflector.scalaTypeOf[Option[Int]]
      params(3).argType must_!= Reflector.scalaTypeOf[Option[String]]
      params(3).argType.typeArgs must_== Seq(Reflector.scalaTypeOf[Int])
      params(4).name must_== "createdAt"
      params(4).defaultValue must beNone
      params(4).argType must_== Reflector.scalaTypeOf[Date]
      params(5).name must_== "deletedAt"
      params(5).defaultValue must beNone
      params(5).argType must_== Reflector.scalaTypeOf[Option[Date]]
      params(5).argType.typeArgs must_== Seq(Reflector.scalaTypeOf[Date])
    }

    "describe a type parameterized class" in {
      val desc = Reflector.describe[RRTypeParam[Int]].asInstanceOf[ClassDescriptor]
      desc.constructors.size must_== 1
      val params = desc.constructors.head.params
      params(0).name must_== "id"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[Int]
      params(1).name must_== "name"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[String]
      params(2).name must_== "value"
      params(2).defaultValue must beNone
      params(2).argType must_== Reflector.scalaTypeOf[Int]
      params(3).name must_== "opt"
      params(3).defaultValue must beNone
      params(3).argType must_== Reflector.scalaTypeOf[Option[Int]]
      params(4).name must_== "seq"
      params(4).defaultValue must beNone
      params(4).argType must_== Reflector.scalaTypeOf[Seq[Int]]
      params(5).name must_== "map"
      params(5).defaultValue must beNone
      params(5).argType must_== Reflector.scalaTypeOf[Map[String, Int]]
    }

    "describe a type with nested generic types" in {
      val desc = Reflector.describe[NestedType].asInstanceOf[ClassDescriptor]
      desc.constructors.size must_== 1
      val params = desc.constructors.head.params
      params(0).name must_== "dat"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[List[Map[Double, Option[Int]]]]
      params(1).name must_== "lis"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]]
    }
  }
}