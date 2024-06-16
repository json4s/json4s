package org.json4s

import java.util.{TimeZone, Date}

/**
 * Conversions between String and Date.
 */
trait DateFormat {
  def parse(s: String): Option[Date]
  def format(d: Date): String
  def timezone: TimeZone
}
