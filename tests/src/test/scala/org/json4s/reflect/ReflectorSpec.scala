package org.json4s.reflect

import org.json4s.reflect
import org.specs2.mutable.Specification

import ReflectorSpec.{Person, Dog, Cat}

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
    }

    "denote the annotated constructor as primary even if multiple exist" in {
      val klass = Reflector.scalaTypeOf(classOf[Cat])
      val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]

      descriptor.constructors.size must_== 3
      descriptor.constructors.count(_.isPrimary) must_== 1
    }
  }
}
