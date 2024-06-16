package org.json4s

import org.json4s.prefs.ExtractionNullStrategy
import org.json4s.prefs.ExtractionNullStrategy.Disallow
import org.scalatest.wordspec.AnyWordSpec

class FormatsBugs extends AnyWordSpec {

  "Formats" should {
    "retain 'extractionNullStrategy' setting over updates" in {
      val f = new DefaultFormats {
        override val extractionNullStrategy: ExtractionNullStrategy = Disallow
      }
      val fModified = f.withBigDecimal
      assert(fModified.extractionNullStrategy == Disallow)
    }
  }

}
