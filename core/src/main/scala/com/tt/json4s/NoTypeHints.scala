package com.tt.json4s

/**
 * Do not use any type hints.
 */
case object NoTypeHints extends TypeHints {
  val hints: List[Class[_]] = Nil
  def hintFor(clazz: Class[_]) = None
  def classFor(hint: String, parent: Class[_]) = None
  override def shouldExtractHints(clazz: Class[_]) = false
}
