package org.json4s

import org.specs2.mutable.Specification

class FormatsBugs extends Specification {

  "Formats" should {
    "retain 'allowNull' setting over updates" in {
      val f = new DefaultFormats {
        override val allowNull: Boolean = false
      }
      val fModified = f.withBigDecimal
      fModified.allowNull must beFalse
    }
  }

}
