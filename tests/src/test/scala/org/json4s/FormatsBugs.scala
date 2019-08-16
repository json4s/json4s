package org.json4s

import org.specs2.mutable.Specification

class FormatsBugs extends Specification {

  "Formats" should {
    "retain 'allowNull' setting over updates" in {
      val f = new DefaultFormats {
        override def allowNull(targetType: Class[_]) = false
      }
      val fModified = f.withBigDecimal
      fModified.allowNull(classOf[String]) must beFalse
    }
  }

}
