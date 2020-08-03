package org.json4s.reflect

import org.json4s.reflect
import org.specs2.mutable.Specification
import ReflectorSpec.{Cat, Dog, Person}
import org.json4s.{DefaultFormats, Formats, JInt, JObject, JString}

object GenericCaseClassWithCompanion {
  def apply[A](v: A): GenericCaseClassWithCompanion[A] = GenericCaseClassWithCompanion(v, "Bar")
}
case class GenericCaseClassWithCompanion[A](value: A, other: String)

object ReflectorSpec {
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
}

class ReflectorSpec extends Specification {

  "Reflector" should {
    "issue 507" in {
      implicit val formats: Formats = DefaultFormats

      val result = org.json4s.Extraction.decompose(
        GenericCaseClassWithCompanion(3)
      )
      result must_== JObject(List(("value", JInt(3)), ("other", JString("Bar"))))
    }

    "discover all constructors, incl. the ones from companion object" in {
      val klass = Reflector.scalaTypeOf(classOf[Person])
      val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]

      // the main one (with firstName, lastName Strings) is seen as two distinct ones:
      // as a constructor and an apply method
      descriptor.constructors.size must_== 4
    }

    "denote no constructor as primary if there are multiple competing" in {
      val klass = Reflector.scalaTypeOf(classOf[Person])
      val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]

      descriptor.constructors.count(_.isPrimary) must_== 0
    }

    "denote the only constructor as primary if only one exists" in {
      val klass = Reflector.scalaTypeOf(classOf[Dog])
      val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]

      // the only human-visible constructor is visible as two - the constructor and the apply method
      descriptor.constructors.size must_== 2
      descriptor.constructors.count(_.isPrimary) must_== 1
      descriptor.constructors(0).isPrimary must_== true
      descriptor.constructors(1).isPrimary must_== false
    }

    "denote the annotated constructor as primary even if multiple exist" in {
      val klass = Reflector.scalaTypeOf(classOf[Cat])
      val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]

      descriptor.constructors.size must_== 3
      descriptor.constructors.count(_.isPrimary) must_== 1
    }

    "retrieve constructors of a class in a deterministic order" in {
      val klass = Reflector.scalaTypeOf(classOf[Person])
      val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]

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
