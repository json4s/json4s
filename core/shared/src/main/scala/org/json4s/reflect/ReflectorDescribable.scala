package org.json4s.reflect

trait ReflectorDescribable[T] {
  def companionClasses: List[(Class[?], AnyRef)]
  def paranamer: ParameterNameReader
  def scalaType: ScalaType
}
