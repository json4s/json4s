package org.json4s.reflect

import java.sql.Timestamp
import java.util.Date

import org.json4s.{DateTime, DefaultFormats, Formats, MappingException, Obj, Objs, reflect}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

case class RRSimple(id: Int, name: String, items: List[String], createdAt: Date)

case class RRSimpleJoda(id: Int, name: String, items: List[String], createdAt: DateTime)

case class RROption(id: Int, name: String, status: Option[String], code: Option[Int], createdAt: Date, deletedAt: Option[Date])

case class RRTypeParam[T](id: Int, name: String, value: T, opt: Option[T], seq: Seq[T], map: Map[String, T])

case class Response(data: List[Map[String, Int]])

case class NestedType(dat: List[Map[Double, Option[Int]]], lis: List[List[List[List[List[Int]]]]])

case class NestedType3(dat: List[Map[Double, Option[List[Option[Int]]]]], lis: List[List[List[List[List[Int]]]]])

case class NestedType4(dat: List[Map[Double, Option[List[Map[Long, Option[Int]]]]]], lis: List[List[List[List[List[Int]]]]])

case class NestedType5(dat: List[Map[Double, Option[List[Map[Long, Option[Map[Byte, Either[Double, Long]]]]]]]], lis: List[List[List[List[List[Int]]]]])

case class NestedResType[T, S, V <: Option[S]](t: T, v: V, dat: List[Map[T, V]], lis: List[List[List[List[List[S]]]]])

case class Person(firstName: String, lastName: String) {
  def this(age: Int) = this("John", "Doe")
}
object Person {
  def apply(email: String) = new Person("Russell", "Westbrook")
}

case class Dog(name: String)

case class Cat @PrimaryConstructor() (name: String) {
  def this(owner: Person) = this(s"${owner.firstName}'s favorite pet'")
}

case object TheObject

object PathTypes {

  type T = Map[String, Double]

  case class TypeAliasOfGenericType(p: T)

  trait WithCaseClass {

    case class FromTrait(name: String)

    case class FromTraitRROption(id: Int, name: String, status: Option[String], code: Option[Int], createdAt: Date, deletedAt: Option[Date])

    // case class FromTraitRRTypeParam[T](id: Int, name: String, value: T, opt: Option[T], seq: Seq[T], map: Map[String, T])
    // ..
  }

  object HasTrait extends WithCaseClass {
    def descr = Reflector.describe[FromTrait]
  }

  class ContainsCaseClass {

    case class InternalType(name: String)

    def methodWithCaseClass = {
      case class InMethod(name: String)
      implicit val formats: Formats = DefaultFormats.withCompanions(classOf[InMethod] -> this)
      Reflector.describe[InMethod]
    }

    def methodWithClosure = {
      val fn = () => {
        case class InFunction(name: String)
        //        val st = Reflector.scalaTypeOf[InFunction] // -> Reflector.describe[InFunction]
        //        val sig = ScalaSigReader.findScalaSig(st.erasure)
        //        val classes = sig.get.symbols.collect({ case c: ClassSymbol => c })
        //        (st, classes)
        Reflector.describe[InFunction]
      }
      fn()
    }
  }


}

class NormalClass {
  val complex: RRSimple = RRSimple(1, "ba", Nil, new Date)
  val string: String = "bla"
  val primitive: Int = 1
  val optPrimitive: Option[Int] = Some(3)
}

class ReflectorSpec extends Specification {
  implicit val formats: Formats = DefaultFormats.withCompanions(
    classOf[PathTypes.HasTrait.FromTrait] -> PathTypes.HasTrait,
    classOf[PathTypes.HasTrait.FromTraitRROption] -> PathTypes.HasTrait
  )

  "Reflector" should {

    val inst = new PathTypes.ContainsCaseClass

    "describe a class defined in a class constructor" in {
      val fmts: Formats = formats.withCompanions(classOf[inst.InternalType] -> inst)
      Reflector.describe(manifest[PathTypes.HasTrait.FromTrait], fmts) match {
        case d: ClassDescriptor =>
          d.constructors must not(beEmpty)
          d.constructors.head.params.size must_== 2
          d.properties.size must_== 1
        case _ => fail("Expected a class descriptor")
      }
    }

    "describe a class defined in a trait constructor" in {
      Reflector.describe[PathTypes.HasTrait.FromTrait] match {
        case d: ClassDescriptor =>
          d.constructors must not(beEmpty)
          d.constructors.head.params.size must_== 2
          d.properties.size must_== 1
          d.companion.map(_.instance) must_== Some(PathTypes.HasTrait.FromTrait)
          d.constructors.head.params(0).defaultValue.get() must_== PathTypes.HasTrait
        case _ => fail("Expected a class descriptor")
      }
    }

    "describe a class defined in a method" in {
      //      inst.methodWithCaseClass match {
      //        case d: ClassDescriptor =>
      //          println(d)
      //          d.constructors must not(beEmpty)
      //          d.constructors.head.params.size must_== 1
      //          d.properties.size must_== 1
      //        case _ => fail("Expected a class descriptor")
      //      }
      inst.methodWithCaseClass must throwA[MappingException]
    }

    "describe a class defined in a closure" in {
      inst.methodWithClosure must throwA[MappingException]
    }
    "describe a case object" in {
      val descr = Reflector.describe(TheObject.getClass).asInstanceOf[ClassDescriptor]
      val res = descr.mostComprehensive must not(throwAn[Exception])
      println(Reflector.describe(TheObject.getClass))
      res
    }

    "describe primitives" in {
      Reflector.describe[Int] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Int])
      Reflector.describe[Byte] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Byte])
      Reflector.describe[Short] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Short])
      Reflector.describe[Long] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Long])
      Reflector.describe[Double] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Double])
      Reflector.describe[Float] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Float])
      Reflector.describe[java.lang.Integer] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Integer])
      Reflector.describe[java.lang.Byte] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Byte])
      Reflector.describe[java.lang.Short] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Short])
      Reflector.describe[java.lang.Long] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Long])
      Reflector.describe[java.lang.Double] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Double])
      Reflector.describe[java.lang.Float] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Float])
      Reflector.describe[BigInt] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[BigInt])
      Reflector.describe[BigDecimal] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[BigDecimal])
      Reflector.describe[java.math.BigInteger] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.math.BigInteger])
      Reflector.describe[java.math.BigDecimal] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[java.math.BigDecimal])
      Reflector.describe[String] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[String])
      Reflector.describe[Date] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Date])
      Reflector.describe[Timestamp] must_== PrimitiveDescriptor(Reflector.scalaTypeOf[Timestamp])
    }

    "Describe a case class with Type Alias of Genric Types" in {
      val desc = Reflector.describe[PathTypes.TypeAliasOfGenericType].asInstanceOf[ClassDescriptor]
      desc.properties(0).returnType must_== Reflector.scalaTypeOf[Map[String, Double]]
    }

    def genericCheckCaseClass(desc: ObjectDescriptor)(params: Seq[ConstructorParamDescriptor] => MatchResult[Any]): MatchResult[Any] = {
      val realDesc = desc.asInstanceOf[ClassDescriptor]

      // One for c'tor, one for apply
      realDesc.constructors.size must_== 2

      params(realDesc.constructors(0).params)
      params(realDesc.constructors(1).params)
    }

    def checkCaseClass[A: Manifest](params: Seq[ConstructorParamDescriptor] => MatchResult[Any]): MatchResult[Any] = {
      val desc = Reflector.describe[A].asInstanceOf[ClassDescriptor]
      genericCheckCaseClass(desc)(params)
    }

    def checkCtorParams(createdAtType: ScalaType)(params: Seq[ConstructorParamDescriptor]): MatchResult[Any] = {
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
      params(3).argType must_== createdAtType
    }

    "describe a simple case class" in checkCaseClass[RRSimple](checkCtorParams(Reflector.scalaTypeOf[Date]))
    "describe a simple joda case class" in checkCaseClass[RRSimpleJoda](checkCtorParams(Reflector.scalaTypeOf[DateTime]))

    "Describe a case class with options" in checkCaseClass[RROption] { params =>
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

    "describe a type parameterized class" in checkCaseClass[RRTypeParam[Int]] { params =>
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

    "describe a type with nested generic types" in checkCaseClass[NestedType] { params =>
      params(0).name must_== "dat"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[List[Map[Double, Option[Int]]]]
      params(1).name must_== "lis"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]]
    }

    "describe a type with nested generic types 2" in checkCaseClass[NestedType3] { params =>
      params(0).name must_== "dat"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[List[Map[Double, Option[List[Option[Int]]]]]]
      params(1).name must_== "lis"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]]
    }

    "describe a type with nested generic types 3" in checkCaseClass[NestedType4] { params =>
      params(0).name must_== "dat"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[List[Map[Double, Option[List[Map[Long, Option[Int]]]]]]]
      params(1).name must_== "lis"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]]
    }

    "describe a type with nested generic types 4" in checkCaseClass[NestedType5] { params =>
      params(0).name must_== "dat"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[List[Map[Double, Option[List[Map[Long, Option[Map[Byte, Either[Double, Long]]]]]]]]]
      params(1).name must_== "lis"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]]
    }

    "describe a type with nested generic types parameters" in checkCaseClass[NestedResType[Double, Int, Option[Int]]] { params =>
      params(0).name must_== "t"
      params(0).defaultValue must beNone
      params(0).argType must_== Reflector.scalaTypeOf[Double]
      params(1).name must_== "v"
      params(1).defaultValue must beNone
      params(1).argType must_== Reflector.scalaTypeOf[Option[Int]]
      params(2).name must_== "dat"
      params(2).defaultValue must beNone
      params(2).argType must_== Reflector.scalaTypeOf[List[Map[Double, Option[Int]]]]
      params(3).name must_== "lis"
      params(3).defaultValue must beNone
      params(3).argType must_== Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]]
    }

    "describe a class with a wildcard parameter" in checkCaseClass[Objs] { params =>
      params(0).name must_== "objects"
      params(0).argType must_== Reflector.scalaTypeOf[List[Obj[_]]]
    }

    "describe the fields of a class" in {
      val desc = Reflector.describe[NormalClass].asInstanceOf[ClassDescriptor]
      desc.constructors.size must_== 1
      val params = desc.properties
      params.size must_== 4
      params(0).name must_== "complex"
      params(0).returnType must_== Reflector.scalaTypeOf[RRSimple]
      params(1).name must_== "string"
      params(1).returnType must_== Reflector.scalaTypeOf[String]
      params(2).name must_== "primitive"
      params(2).returnType must_== Reflector.scalaTypeOf[Int]
      params(3).name must_== "optPrimitive"
      params(3).returnType must_== Reflector.scalaTypeOf[Option[Int]]
    }

    "Describe a case class with $outer field" in {
      val desc = Reflector.describe[PathTypes.HasTrait.FromTraitRROption].asInstanceOf[ClassDescriptor]
      desc.companion.map(_.instance) must_== Some(PathTypes.HasTrait.FromTraitRROption)
      desc.constructors.head.params(0).defaultValue.get() must_== PathTypes.HasTrait
    }

    "Describe a case class with options defined in a trait" in {
      checkCaseClass[PathTypes.HasTrait.FromTraitRROption]{ params =>
        val ctorParams = params.filterNot(_.name == ScalaSigReader.OuterFieldName)
        ctorParams(0).name must_== "id"
        ctorParams(0).defaultValue must beNone
        ctorParams(0).argType must_== Reflector.scalaTypeOf[Int]
        ctorParams(1).name must_== "name"
        ctorParams(1).defaultValue must beNone
        ctorParams(1).argType must_== Reflector.scalaTypeOf[String]
        ctorParams(2).name must_== "status"
        ctorParams(2).defaultValue must beNone
        ctorParams(2).argType must_== Reflector.scalaTypeOf[Option[String]]
        ctorParams(2).argType.typeArgs must_== Seq(Reflector.scalaTypeOf[String])
        ctorParams(3).name must_== "code"
        ctorParams(3).defaultValue must beNone
        ctorParams(3).argType must_== Reflector.scalaTypeOf[Option[Int]]
        ctorParams(3).argType must_!= Reflector.scalaTypeOf[Option[String]]
        ctorParams(3).argType.typeArgs must_== Seq(Reflector.scalaTypeOf[Int])
        ctorParams(4).name must_== "createdAt"
        ctorParams(4).defaultValue must beNone
        ctorParams(4).argType must_== Reflector.scalaTypeOf[Date]
        ctorParams(5).name must_== "deletedAt"
        ctorParams(5).defaultValue must beNone
        ctorParams(5).argType must_== Reflector.scalaTypeOf[Option[Date]]
        ctorParams(5).argType.typeArgs must_== Seq(Reflector.scalaTypeOf[Date])
      }
    }

    "discover all constructors, incl. the ones from companion object" in {
      val klass = Reflector.scalaTypeOf(classOf[Person])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      // the main one (with firstName, lastName Strings) is seen as two distinct ones:
      // as a constructor and an apply method
      descriptor.constructors.size must_== 4
    }

    "denote no constructor as primary if there are multiple competing" in {
      val klass = Reflector.scalaTypeOf(classOf[Person])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      descriptor.constructors.count(_.isPrimary) must_== 0
    }

    "denote the only constructor as primary if only one exists" in {
      val klass = Reflector.scalaTypeOf(classOf[Dog])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      // the only human-visible constructor is visible as two - the constructor and the apply method
      descriptor.constructors.size must_== 2
      descriptor.constructors.count(_.isPrimary) must_== 1
      descriptor.constructors(0).isPrimary must_== true
      descriptor.constructors(1).isPrimary must_== false
    }

    "denote the annotated constructor as primary even if multiple exist" in {
      val klass = Reflector.scalaTypeOf(classOf[Cat])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      descriptor.constructors.size must_== 3
      descriptor.constructors.count(_.isPrimary) must_== 1
    }

    "retrieve constructors of a class in a deterministic order" in {
      val klass = Reflector.scalaTypeOf(classOf[Person])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      descriptor.constructors.size must_== 4
      val first = descriptor.constructors(0)
      val second = descriptor.constructors(1)
      val third = descriptor.constructors(2)
      val fourth = descriptor.constructors(3)

      first.params.map(_.name) must_== Seq("firstName", "lastName")
      first.constructor.method must_== null
      first.constructor.constructor must_!= null

      second.params.map(_.name) must_== Seq("age")

      third.params.map(_.name) must_== Seq("firstName", "lastName")
      third.constructor.method must_!= null
      third.constructor.constructor must_== null

      fourth.params.map(_.name) must_== Seq("email")
    }
  }
}
