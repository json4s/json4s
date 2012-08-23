package org.json4s

import java.util.Date

/** Conversions between String and Date.
 */
trait DateFormat[T] {
  def parse(s: String): Option[T]
  def format(d: T): String
}