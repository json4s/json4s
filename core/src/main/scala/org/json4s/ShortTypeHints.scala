package org.json4s

/**
 * Use short class name as a type hint.
 */
case class ShortTypeHints(hints: List[Class[_]], override val typeHintFieldName: String = "jsonClass")
  extends TypeHints {
  def hintFor(clazz: Class[_]): Option[String] =
    Some(clazz.getName.substring(clazz.getName.lastIndexOf(".") + 1))
  def classFor(hint: String, parent: Class[_]) = hints find (hintFor(_).exists(_ == hint))
}
