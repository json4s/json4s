package org.json4s.prefs

import org.json4s.StringTransformations

/** key transformation between snake case and camel case.
 */
trait KeyTransformation {
  def isIdentity: Boolean
  def write(s: String): String
  def read(s: String): String
}

/** default policy do nothing with keys
  */
object IdentityTransformation extends KeyTransformation {
  override val isIdentity = true
  override def write(s: String): String = s
  override def read(s: String): String = s
}

/** Java/Scala to JS standard transformation
  * read json as snake_case and transform key to camelCase
  */
object CamelSnakeTransformation extends KeyTransformation {
  override val isIdentity = false
  override def write(s: String): String = StringTransformations.underscore(s)
  override def read(s: String): String = StringTransformations.camelize(s)
}


