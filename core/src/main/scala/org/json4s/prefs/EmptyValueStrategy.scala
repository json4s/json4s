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
  implicit val default = Skip.skip

  /**
   * Skip empty fields and sequence items.
   */
  object Skip {
    implicit val skip = new EmptyValueStrategy {
      def noneValReplacement = None

      def apply(value: JValue) = value
    }
  }

  /**
   * Preserve empty fields and sequence items as "null".
   */
  object Preserve {
    implicit val preserve = new EmptyValueStrategy {

      def noneValReplacement = Some(JNull)

      def apply(value: JValue): JValue = value match {
        case JArray(items) => JArray(items map apply)
        case JObject(fields) => JObject(fields map {
          case JField(name, value) => JField(name, apply(value))
        })
        case JNothing => JNull
        case oth => oth
      }
    }
  }

}

/**
 * Strategy for empty values treatment.
 */
trait EmptyValueStrategy {

  def noneValReplacement: Option[AnyRef]

  def apply(elem: JValue): JValue

}
