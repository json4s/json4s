package org.json4s

/**
 * Do not use any type hints.
 */
case object NoTypeHints extends TypeHints {
  val hints: List[Class[?]] = Nil
  def hintFor(clazz: Class[?]): Option[String] = None
  def classFor(hint: String, parent: Class[?]): Option[Class[?]] = None
  override def shouldExtractHints(clazz: Class[?]) = false
}
