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
}
