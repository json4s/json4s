package org.json4s.prefs

object ExtractionNullStrategy {

  /**
   * Default behaviour - keep null values
   */
  case object Keep extends ExtractionNullStrategy

  /**
   * Fail if null values are encountered
   */
  case object Disallow extends ExtractionNullStrategy

  /**
   * Filters out null values, as if they were not present
   */
  case object TreatAsAbsent extends ExtractionNullStrategy
}

sealed abstract class ExtractionNullStrategy extends Product with Serializable
