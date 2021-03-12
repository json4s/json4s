package org.json4s.reflect

import org.json4s.reflect.DescriptorsSpec.{Company, Citizen, Human}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

object DescriptorsSpec {
  case class Human(firstName: String, lastName: String) {
    def this(age: Int) = this("John", "Doe")
  }
  object Human {
    def apply(email: String) = new Human("Russell", "Westbrook")
  }

  case class Citizen(firstName: String, lastName: String, id: String) {
    def this(firstName: String, lastName: String, idAsANumber: Int) = this(firstName, lastName, idAsANumber.toString)
  }

  case class Company(name: String, industry: String = "IT", ceo: Human = new Human("My", "Self"), yearFounded: Int) {
    def this(name: String, industry: String, ceo: Human) = {
      this(name, industry, ceo, 2000)
    }
    def this(name: String, industry: String, ceo: Human, yearFounded: Option[Int]) = {
      this(name, industry, ceo, yearFounded.getOrElse(2010))
    }
  }
}

class DescriptorsSpec extends AnyWordSpec {

  "descriptors.bestMatching" should {
    "pick the natural one if arg names match exactly" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Human])

      // when
      val best = descriptor.bestMatching(List("firstName", "lastName")).get.constructor

      // test
      assert(best.constructor != null)
      best.method shouldBe null
      best.getParameterTypes() shouldBe Array(classOf[String], classOf[String])
    }

    "pick the one matching argument names" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Citizen])

      // when
      val variant1 = descriptor.bestMatching(List("firstName", "lastName", "id")).get.constructor
      val variant2 = descriptor.bestMatching(List("firstName", "lastName", "idAsANumber")).get.constructor

      // test
      variant1.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[String])
      variant2.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[Int])
    }

    "pick the most specific one (i.e. skipping defaults) if values are given" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Company])

      // when
      val best = descriptor.bestMatching(List("name", "industry", "yearFounded")).get.constructor

      // test
      best.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[Human], classOf[Int])
    }

    "pick the most specific one (i.e. skipping defaults) if values are given, and some extras are given" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Company])

      // when
      val best = descriptor.bestMatching(List("name", "industry", "yearFounded", "nonExistingProperty")).get.constructor

      // test
      best.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[Human], classOf[Int])
    }

    "pick the one using default value, not option if a value is not given" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Company])

      // when
      val best = descriptor.bestMatching(List("name", "industry")).get.constructor

      // test
      best.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[Human], classOf[Int])
    }

    "pick the main one if not all values are provided" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Citizen])

      // when
      val best = descriptor.bestMatching(List("firstName", "lastName")).get.constructor

      // test
      best.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[String])
    }

    "pick the main one if not all values are provided and some extras are provided" in {
      // given
      val descriptor: ClassDescriptor = describe(classOf[Citizen])

      // when
      val best = descriptor.bestMatching(List("firstName", "lastName", "newInfoWhichDoesNotExist")).get.constructor

      // test
      best.getParameterTypes() shouldBe Array(classOf[String], classOf[String], classOf[String])
    }
  }

  private def describe(clazz: Class[_]) = {
    Reflector.describe(Reflector.scalaTypeOf(clazz)).asInstanceOf[ClassDescriptor]
  }
}
