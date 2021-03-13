package org.json4s.reflect

import java.sql.Timestamp
import java.util.Date

import org.json4s.{DateTime, DefaultFormats, Formats, JInt, JObject, JString, MappingException, Obj, Objs, reflect}
import org.scalatest.Assertion
import org.scalatest.wordspec.AnyWordSpec

case class RRSimple(id: Int, name: String, items: List[String], createdAt: Date)

case class RRSimpleJoda(id: Int, name: String, items: List[String], createdAt: DateTime)

case class RROption(
  id: Int,
  name: String,
  status: Option[String],
  code: Option[Int],
  createdAt: Date,
  deletedAt: Option[Date]
)

case class RRTypeParam[T](id: Int, name: String, value: T, opt: Option[T], seq: Seq[T], map: Map[String, T])

case class Response(data: List[Map[String, Int]])

case class NestedType(dat: List[Map[Double, Option[Int]]], lis: List[List[List[List[List[Int]]]]])

case class NestedType3(dat: List[Map[Double, Option[List[Option[Int]]]]], lis: List[List[List[List[List[Int]]]]])

case class NestedType4(
  dat: List[Map[Double, Option[List[Map[Long, Option[Int]]]]]],
  lis: List[List[List[List[List[Int]]]]]
)

case class NestedType5(
  dat: List[Map[Double, Option[List[Map[Long, Option[Map[Byte, Either[Double, Long]]]]]]]],
  lis: List[List[List[List[List[Int]]]]]
)

case class NestedResType[T, S, V <: Option[S]](t: T, v: V, dat: List[Map[T, V]], lis: List[List[List[List[List[S]]]]])

case object TheObject

object PathTypes {

  type T = Map[String, Double]

  case class TypeAliasOfGenericType(p: T)

  trait WithCaseClass {

    case class FromTrait(name: String)

    case class FromTraitRROption(
      id: Int,
      name: String,
      status: Option[String],
      code: Option[Int],
      createdAt: Date,
      deletedAt: Option[Date]
    )

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

case class PetOwner(firstName: String, lastName: String) {
  def this(age: Int) = this("John", "Doe")
}
object PetOwner {
  def apply(email: String) = new PetOwner("Russell", "Westbrook")
}

case class Dog(name: String)

case class Cat @PrimaryConstructor() (name: String) {
  def this(owner: PetOwner) = this(s"${owner.firstName}'s favorite pet'")
}

object GenericCaseClassWithCompanion {
  def apply[A](v: A): GenericCaseClassWithCompanion[A] = GenericCaseClassWithCompanion(v, "Bar")
}
case class GenericCaseClassWithCompanion[A](value: A, other: String)

class ReflectorSpec extends AnyWordSpec {
  implicit val formats: Formats = DefaultFormats.withCompanions(
    classOf[PathTypes.HasTrait.FromTrait] -> PathTypes.HasTrait,
    classOf[PathTypes.HasTrait.FromTraitRROption] -> PathTypes.HasTrait
  )

  "Reflector" should {

    val inst = new PathTypes.ContainsCaseClass

    "issue 507" in {
      val result = org.json4s.Extraction.decompose(
        GenericCaseClassWithCompanion(3)
      )
      assert(result == JObject(List(("value", JInt(3)), ("other", JString("Bar")))))
    }

    "describe a class defined in a class constructor" in {
      val fmts: Formats = formats.withCompanions(classOf[inst.InternalType] -> inst)
      Reflector.describe(manifest[PathTypes.HasTrait.FromTrait], fmts) match {
        case d: ClassDescriptor =>
          assert(d.constructors.nonEmpty)
          assert(d.constructors.head.params.size == 2)
          assert(d.properties.size == 1)
        case _ => fail("Expected a class descriptor")
      }
    }

    "describe a class defined in a trait constructor" in {
      Reflector.describe[PathTypes.HasTrait.FromTrait] match {
        case d: ClassDescriptor =>
          assert(d.constructors.nonEmpty)
          assert(d.constructors.head.params.size == 2)
          assert(d.properties.size == 1)
          assert(d.companion.map(_.instance) == Some(PathTypes.HasTrait.FromTrait))
          assert(d.constructors.head.params(0).defaultValue.get() == PathTypes.HasTrait)
        case _ => fail("Expected a class descriptor")
      }
    }

    "describe a class defined in a method" in {
      //      inst.methodWithCaseClass match {
      //        case d: ClassDescriptor =>
      //          println(d)
      //          assert(d.constructors.nonEmpty)
      //          d.constructors.head.params.size must_== 1
      //          d.properties.size must_== 1
      //        case _ => fail("Expected a class descriptor")
      //      }
      assertThrows[MappingException] { inst.methodWithCaseClass }
    }

    "describe a class defined in a closure" in {
      assertThrows[MappingException] { inst.methodWithClosure }
    }
    "describe a case object" in {
      val descr = Reflector.describe(TheObject.getClass).asInstanceOf[ClassDescriptor]
      val res = descr.mostComprehensive
      println(Reflector.describe(TheObject.getClass))
      res
    }

    "describe primitives" in {
      assert(Reflector.describe[Int] == PrimitiveDescriptor(Reflector.scalaTypeOf[Int]))
      assert(Reflector.describe[Byte] == PrimitiveDescriptor(Reflector.scalaTypeOf[Byte]))
      assert(Reflector.describe[Short] == PrimitiveDescriptor(Reflector.scalaTypeOf[Short]))
      assert(Reflector.describe[Long] == PrimitiveDescriptor(Reflector.scalaTypeOf[Long]))
      assert(Reflector.describe[Double] == PrimitiveDescriptor(Reflector.scalaTypeOf[Double]))
      assert(Reflector.describe[Float] == PrimitiveDescriptor(Reflector.scalaTypeOf[Float]))
      assert(Reflector.describe[java.lang.Integer] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Integer]))
      assert(Reflector.describe[java.lang.Byte] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Byte]))
      assert(Reflector.describe[java.lang.Short] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Short]))
      assert(Reflector.describe[java.lang.Long] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Long]))
      assert(Reflector.describe[java.lang.Double] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Double]))
      assert(Reflector.describe[java.lang.Float] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.lang.Float]))
      assert(Reflector.describe[BigInt] == PrimitiveDescriptor(Reflector.scalaTypeOf[BigInt]))
      assert(Reflector.describe[BigDecimal] == PrimitiveDescriptor(Reflector.scalaTypeOf[BigDecimal]))
      assert(
        Reflector.describe[java.math.BigInteger] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.math.BigInteger])
      )
      assert(
        Reflector.describe[java.math.BigDecimal] == PrimitiveDescriptor(Reflector.scalaTypeOf[java.math.BigDecimal])
      )
      assert(Reflector.describe[String] == PrimitiveDescriptor(Reflector.scalaTypeOf[String]))
      assert(Reflector.describe[Date] == PrimitiveDescriptor(Reflector.scalaTypeOf[Date]))
      assert(Reflector.describe[Timestamp] == PrimitiveDescriptor(Reflector.scalaTypeOf[Timestamp]))
    }

    "Describe a case class with Type Alias of Genric Types" in {
      val desc = Reflector.describe[PathTypes.TypeAliasOfGenericType].asInstanceOf[ClassDescriptor]
      assert(desc.properties(0).returnType == Reflector.scalaTypeOf[Map[String, Double]])
    }

    def genericCheckCaseClass(
      desc: ObjectDescriptor
    )(params: Seq[ConstructorParamDescriptor] => Assertion): Assertion = {
      val realDesc = desc.asInstanceOf[ClassDescriptor]

      // One for c'tor, one for apply
      assert(realDesc.constructors.size == 2)

      params(realDesc.constructors(0).params)
      params(realDesc.constructors(1).params)
    }

    def checkCaseClass[A: Manifest](params: Seq[ConstructorParamDescriptor] => Assertion): Assertion = {
      val desc = Reflector.describe[A].asInstanceOf[ClassDescriptor]
      genericCheckCaseClass(desc)(params)
    }

    def checkCtorParams(createdAtType: ScalaType)(params: Seq[ConstructorParamDescriptor]): Assertion = {
      assert(params(0).name == "id")
      assert(params(0).defaultValue.isEmpty)
      assert(params(0).argType == Reflector.scalaTypeOf[Int])
      assert(params(1).name == "name")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[String])
      assert(params(2).name == "items")
      assert(params(2).defaultValue.isEmpty)
      assert(params(2).argType == Reflector.scalaTypeOf[List[String]])
      assert(params(3).name == "createdAt")
      assert(params(3).defaultValue.isEmpty)
      assert(params(3).argType == createdAtType)
    }

    "describe a simple case class" in checkCaseClass[RRSimple](checkCtorParams(Reflector.scalaTypeOf[Date]))
    "describe a simple joda case class" in checkCaseClass[RRSimpleJoda](
      checkCtorParams(Reflector.scalaTypeOf[DateTime])
    )

    "Describe a case class with options" in checkCaseClass[RROption] { params =>
      assert(params(0).name == "id")
      assert(params(0).defaultValue.isEmpty)
      assert(params(0).argType == Reflector.scalaTypeOf[Int])
      assert(params(1).name == "name")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[String])
      assert(params(2).name == "status")
      assert(params(2).defaultValue.isEmpty)
      assert(params(2).argType == Reflector.scalaTypeOf[Option[String]])
      assert(params(2).argType.typeArgs == Seq(Reflector.scalaTypeOf[String]))
      assert(params(3).name == "code")
      assert(params(3).defaultValue.isEmpty)
      assert(params(3).argType == Reflector.scalaTypeOf[Option[Int]])
      assert(params(3).argType != Reflector.scalaTypeOf[Option[String]])
      assert(params(3).argType.typeArgs == Seq(Reflector.scalaTypeOf[Int]))
      assert(params(4).name == "createdAt")
      assert(params(4).defaultValue.isEmpty)
      assert(params(4).argType == Reflector.scalaTypeOf[Date])
      assert(params(5).name == "deletedAt")
      assert(params(5).defaultValue.isEmpty)
      assert(params(5).argType == Reflector.scalaTypeOf[Option[Date]])
      assert(params(5).argType.typeArgs == Seq(Reflector.scalaTypeOf[Date]))
    }

    "describe a type parameterized class" in checkCaseClass[RRTypeParam[Int]] { params =>
      assert(params(0).name == "id")
      assert(params(0).defaultValue.isEmpty)
      assert(params(0).argType == Reflector.scalaTypeOf[Int])
      assert(params(1).name == "name")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[String])
      assert(params(2).name == "value")
      assert(params(2).defaultValue.isEmpty)
      assert(params(2).argType == Reflector.scalaTypeOf[Int])
      assert(params(3).name == "opt")
      assert(params(3).defaultValue.isEmpty)
      assert(params(3).argType == Reflector.scalaTypeOf[Option[Int]])
      assert(params(4).name == "seq")
      assert(params(4).defaultValue.isEmpty)
      assert(params(4).argType == Reflector.scalaTypeOf[Seq[Int]])
      assert(params(5).name == "map")
      assert(params(5).defaultValue.isEmpty)
      assert(params(5).argType == Reflector.scalaTypeOf[Map[String, Int]])
    }

    "describe a type with nested generic types" in checkCaseClass[NestedType] { params =>
      assert(params(0).name == "dat")
      assert(params(0).defaultValue.isEmpty)
      assert(params(0).argType == Reflector.scalaTypeOf[List[Map[Double, Option[Int]]]])
      assert(params(1).name == "lis")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]])
    }

    "describe a type with nested generic types 2" in checkCaseClass[NestedType3] { params =>
      assert(params(0).name == "dat")
      assert(params(0).defaultValue.isEmpty)
      assert(params(0).argType == Reflector.scalaTypeOf[List[Map[Double, Option[List[Option[Int]]]]]])
      assert(params(1).name == "lis")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]])
    }

    "describe a type with nested generic types 3" in checkCaseClass[NestedType4] { params =>
      assert(params(0).name == "dat")
      assert(params(0).defaultValue.isEmpty)
      assert(params(0).argType == Reflector.scalaTypeOf[List[Map[Double, Option[List[Map[Long, Option[Int]]]]]]])
      assert(params(1).name == "lis")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]])
    }

    "describe a type with nested generic types 4" in checkCaseClass[NestedType5] { params =>
      assert(params(0).name == "dat")
      assert(params(0).defaultValue.isEmpty)
      assert(
        params(0).argType == Reflector
          .scalaTypeOf[List[Map[Double, Option[List[Map[Long, Option[Map[Byte, Either[Double, Long]]]]]]]]]
      )
      assert(params(1).name == "lis")
      assert(params(1).defaultValue.isEmpty)
      assert(params(1).argType == Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]])
    }

    "describe a type with nested generic types parameters" in checkCaseClass[NestedResType[Double, Int, Option[Int]]] {
      params =>
        assert(params(0).name == "t")
        assert(params(0).defaultValue.isEmpty)
        assert(params(0).argType == Reflector.scalaTypeOf[Double])
        assert(params(1).name == "v")
        assert(params(1).defaultValue.isEmpty)
        assert(params(1).argType == Reflector.scalaTypeOf[Option[Int]])
        assert(params(2).name == "dat")
        assert(params(2).defaultValue.isEmpty)
        assert(params(2).argType == Reflector.scalaTypeOf[List[Map[Double, Option[Int]]]])
        assert(params(3).name == "lis")
        assert(params(3).defaultValue.isEmpty)
        assert(params(3).argType == Reflector.scalaTypeOf[List[List[List[List[List[Int]]]]]])
    }

    "describe a class with a wildcard parameter" in checkCaseClass[Objs] { params =>
      assert(params(0).name == "objects")
      assert(params(0).argType == Reflector.scalaTypeOf[List[Obj[_]]])
    }

    "describe the fields of a class" in {
      val desc = Reflector.describe[NormalClass].asInstanceOf[ClassDescriptor]
      assert(desc.constructors.size == 1)
      val params = desc.properties
      assert(params.size == 4)
      assert(params(0).name == "complex")
      assert(params(0).returnType == Reflector.scalaTypeOf[RRSimple])
      assert(params(1).name == "string")
      assert(params(1).returnType == Reflector.scalaTypeOf[String])
      assert(params(2).name == "primitive")
      assert(params(2).returnType == Reflector.scalaTypeOf[Int])
      assert(params(3).name == "optPrimitive")
      assert(params(3).returnType == Reflector.scalaTypeOf[Option[Int]])
    }

    "Describe a case class with $outer field" in {
      val desc = Reflector.describe[PathTypes.HasTrait.FromTraitRROption].asInstanceOf[ClassDescriptor]
      assert(desc.companion.map(_.instance) == Some(PathTypes.HasTrait.FromTraitRROption))
      assert(desc.constructors.head.params(0).defaultValue.get() == PathTypes.HasTrait)
    }

    "Describe a case class with options defined in a trait" in {
      checkCaseClass[PathTypes.HasTrait.FromTraitRROption] { params =>
        val ctorParams = params.filterNot(_.name == ScalaSigReader.OuterFieldName)
        assert(ctorParams(0).name == "id")
        assert(ctorParams(0).defaultValue.isEmpty)
        assert(ctorParams(0).argType == Reflector.scalaTypeOf[Int])
        assert(ctorParams(1).name == "name")
        assert(ctorParams(1).defaultValue.isEmpty)
        assert(ctorParams(1).argType == Reflector.scalaTypeOf[String])
        assert(ctorParams(2).name == "status")
        assert(ctorParams(2).defaultValue.isEmpty)
        assert(ctorParams(2).argType == Reflector.scalaTypeOf[Option[String]])
        assert(ctorParams(2).argType.typeArgs == Seq(Reflector.scalaTypeOf[String]))
        assert(ctorParams(3).name == "code")
        assert(ctorParams(3).defaultValue.isEmpty)
        assert(ctorParams(3).argType == Reflector.scalaTypeOf[Option[Int]])
        assert(ctorParams(3).argType != Reflector.scalaTypeOf[Option[String]])
        assert(ctorParams(3).argType.typeArgs == Seq(Reflector.scalaTypeOf[Int]))
        assert(ctorParams(4).name == "createdAt")
        assert(ctorParams(4).defaultValue.isEmpty)
        assert(ctorParams(4).argType == Reflector.scalaTypeOf[Date])
        assert(ctorParams(5).name == "deletedAt")
        assert(ctorParams(5).defaultValue.isEmpty)
        assert(ctorParams(5).argType == Reflector.scalaTypeOf[Option[Date]])
        assert(ctorParams(5).argType.typeArgs == Seq(Reflector.scalaTypeOf[Date]))
      }
    }

    "discover all constructors, incl. the ones from companion object" in {
      val klass = Reflector.scalaTypeOf(classOf[PetOwner])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      // the main one (with firstName, lastName Strings) is seen as two distinct ones:
      // as a constructor and an apply method
      assert(descriptor.constructors.size == 4)
    }

    "denote no constructor as primary if there are multiple competing" in {
      val klass = Reflector.scalaTypeOf(classOf[PetOwner])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      assert(descriptor.constructors.count(_.isPrimary) == 0)
    }

    "denote the only constructor as primary if only one exists" in {
      val klass = Reflector.scalaTypeOf(classOf[Dog])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      // the only human-visible constructor is visible as two - the constructor and the apply method
      assert(descriptor.constructors.size == 2)
      assert(descriptor.constructors.count(_.isPrimary) == 1)
      assert(descriptor.constructors(0).isPrimary == true)
      assert(descriptor.constructors(1).isPrimary == false)
    }

    "denote the annotated constructor as primary even if multiple exist" in {
      val klass = Reflector.scalaTypeOf(classOf[Cat])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      assert(descriptor.constructors.size == 3)
      assert(descriptor.constructors.count(_.isPrimary) == 1)
    }

    "retrieve constructors of a class in a deterministic order" in {
      val klass = Reflector.scalaTypeOf(classOf[PetOwner])
      val descriptor = Reflector.describeWithFormats(klass).asInstanceOf[reflect.ClassDescriptor]

      assert(descriptor.constructors.size == 4)
      val first = descriptor.constructors(0)
      val second = descriptor.constructors(1)
      val third = descriptor.constructors(2)
      val fourth = descriptor.constructors(3)

      assert(first.params.map(_.name) == Seq("firstName", "lastName"))
      assert(first.constructor.method == null)
      assert(first.constructor.constructor != null)

      assert(second.params.map(_.name) == Seq("age"))

      assert(third.params.map(_.name) == Seq("firstName", "lastName"))
      assert(third.constructor.method != null)
      assert(third.constructor.constructor == null)

      assert(fourth.params.map(_.name) == Seq("email"))
    }
  }
}
