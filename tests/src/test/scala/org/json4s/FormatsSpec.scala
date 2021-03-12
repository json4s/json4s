package org.json4s

import org.scalatest.wordspec.AnyWordSpec

class FormatsSpec extends AnyWordSpec {

  "Formats" should {
    "be a Serializable" in {
      val f = new Formats {
        def dateFormat: DateFormat = ???
      }
      assert(f.isInstanceOf[Serializable])
    }
  }

  "DefaultFormats" should {
    "be a Serializable" in {
      assert(DefaultFormats.isInstanceOf[Serializable])
    }
  }

  "ClassDelta NPE Issue#342" should {
    "Check for null class1 based on recursive call to clazz.getSuperclass" in {
      assert(ClassDelta.delta(null, classOf[Object]) == 1)
    }
    "Check for null class2 based on recursive call to clazz.getSuperclass" in {
      assert(ClassDelta.delta(classOf[Object], null) == -1)
    }
  }

  // https://github.com/json4s/json4s/issues/550
  // https://github.com/scala/bug/issues/9948
  "issue#550 avoid scalac bug" in {
    val a = new TypeHints {
      override val hints: List[Class[_]] = List.empty
      override def hintFor(clazz: Class[_]): Option[String] = None
      override def classFor(hint: String, parent: Class[_]): Option[Class[_]] = None
    }
    a + a
  }
}
