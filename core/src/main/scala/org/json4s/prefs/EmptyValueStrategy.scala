package org.json4s.prefs

import org.json4s.{ JValue, JArray, JField, JObject, JNothing, JNull }
import org.json4s.JNull

/**
 * Strategies for empty values treatment.
 *
 * @author marcinkubala
 */
object EmptyValueStrategy {

  /**
   * Skip empty fields/sequence items by default.
   */
  def default = skip

  /**
   * Skip empty fields and sequence items.
   */
  val skip = new EmptyValueStrategy {
    def noneValReplacement = None

    def replaceEmpty(value: JValue) = value
  }

  /**
   * Preserve empty fields and sequence items as "null".
   */
  val preserve = new EmptyValueStrategy {

    def noneValReplacement = Some(JNull)

    def replaceEmpty(value: JValue): JValue = value match {
      case JArray(items) => JArray(items map replaceEmpty)
      case JObject(fields) => JObject(fields map {
        case JField(name, value) => JField(name, replaceEmpty(value))
      })
      case JNothing => JNull
      case oth => oth
    }
  }

}

/**
 * Strategy for empty values treatment.
 */
trait EmptyValueStrategy {

  def noneValReplacement: Option[AnyRef]

  def replaceEmpty(elem: JValue): JValue

}
