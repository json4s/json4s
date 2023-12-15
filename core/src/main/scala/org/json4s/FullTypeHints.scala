package org.json4s

import org.json4s.reflect.Reflector

/**
 * Use full class name as a type hint.
 */
case class FullTypeHints(hints: List[Class[_]], override val typeHintFieldName: String = "jsonClass")
  extends TypeHints {
  def hintFor(clazz: Class[_]): Option[String] = Some(clazz.getName)
  def classFor(hint: String, parent: Class[_]) = {
    Reflector
      .scalaTypeOf(hint)
      .find(h => hints.exists(l => l.isAssignableFrom(h.erasure)))
      .map(_.erasure)
  }
}
