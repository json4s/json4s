package org.json4s.prefs

import org.json4s.JArray
import org.json4s.JField
import org.json4s.JNothing
import org.json4s.JNull
import org.json4s.JObject
import org.json4s.JValue

/**
 * Strategies for empty values treatment.
 *
 * @author Marcin Kubala
 */
object EmptyValueStrategy {

  /**
   * Default behaviour - skip empty fields and sequence items.
   */
  def default: EmptyValueStrategy = skip

  /**
   * Skip empty fields and sequence items.
   */
  val skip: EmptyValueStrategy = new EmptyValueStrategy {
    def noneValReplacement: Option[AnyRef] = None

    def replaceEmpty(value: JValue) = value
  }

  /**
   * Preserve empty fields and sequence items as "null".
   */
  val preserve: EmptyValueStrategy = new EmptyValueStrategy {

    override val noneValReplacement: Option[AnyRef] = Some(JNull)

    def replaceEmpty(value: JValue): JValue = value match {
      case JArray(items) => JArray(items map replaceEmpty)
      case JObject(fields) =>
        JObject(fields map { case JField(name, value) =>
          JField(name, replaceEmpty(value))
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
