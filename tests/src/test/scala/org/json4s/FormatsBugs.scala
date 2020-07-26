package org.json4s

import org.json4s.prefs.ExtractionNullStrategy
import org.json4s.prefs.ExtractionNullStrategy.Disallow
import org.specs2.mutable.Specification

class FormatsBugs extends Specification {

  "Formats" should {
    "retain 'extractionNullStrategy' setting over updates" in {
      val f = new DefaultFormats {
        override val extractionNullStrategy: ExtractionNullStrategy = Disallow
      }
      val fModified = f.withBigDecimal
      fModified.extractionNullStrategy mustEqual Disallow
    }
  }

}
