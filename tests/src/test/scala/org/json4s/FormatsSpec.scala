package org.json4s

import org.specs2.mutable.Specification

class FormatsSpec extends Specification {

  "Formats" should {
    "be a Serializable" in {
      val f = new Formats {
        def dateFormat: DateFormat = ???
      }
      f.isInstanceOf[Serializable] must beTrue
    }
  }

  "DefaultFormats" should {
    "be a Serializable" in {
      DefaultFormats.isInstanceOf[Serializable] must beTrue
    }
  }

  "ClassDelta NPE Issue#342" should {
    "Check for null class1 based on recursive call to clazz.getSuperclass" in {
      ClassDelta.delta(null, classOf[Object]) must be_==(1)
    }
    "Check for null class2 based on recursive call to clazz.getSuperclass" in {
      ClassDelta.delta(classOf[Object], null) must be_==(-1)
    }
  }

  // https://github.com/json4s/json4s/issues/550
  // https://github.com/scala/bug/issues/9948
  "issue#550 avoid scalac bug" in {
    val a = new TypeHints {
      override val hints: List[Class[_]] = List.empty
      override def hintFor(clazz: Class[_]): Option[String] = None
      override def classFor(hint: String): Option[Class[_]] = None
    }
    a + a
    success
  }
}
