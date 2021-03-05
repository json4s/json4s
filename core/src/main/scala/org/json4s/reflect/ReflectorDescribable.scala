package org.json4s.reflect

trait ReflectorDescribable[T] {
  def companionClasses: List[(Class[_], AnyRef)]
  def paranamer: ParameterNameReader
  def scalaType: ScalaType
}
