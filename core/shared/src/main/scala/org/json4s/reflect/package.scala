package org.json4s

import scala.annotation.tailrec

package object reflect {

  def safeSimpleName(clazz: Class[?]) =
    try {
      clazz.getSimpleName
    } catch {
      case _: Throwable =>
        val packageNameLen = Some(clazz.getPackage) map (_.getName.length + 1) getOrElse 0
        stripDollar(clazz.getName.substring(packageNameLen))
    }

  @tailrec
  def stripDollar(name: String): String = {
    val index = name.lastIndexOf('$')
    if (index == -1) {
      name
    } else if (index == name.length - 1) {
      stripDollar(name.substring(0, index))
    } else {
      name.substring(index + 1)
    }
  }

  private[reflect] val ConstructorDefaultValuePattern = "$lessinit$greater$default$%d"
  private[reflect] val ModuleFieldName = "MODULE$"
  private[reflect] val ClassLoaders = Vector(getClass.getClassLoader, Thread.currentThread().getContextClassLoader)

  implicit def scalaTypeDescribable(
    t: ScalaType
  )(implicit formats: Formats = DefaultFormats): ReflectorDescribable[ScalaType] = new ReflectorDescribable[ScalaType] {
    val companionClasses: List[(Class[?], AnyRef)] = formats.companions
    val paranamer: ParameterNameReader = formats.parameterNameReader
    val scalaType: ScalaType = t
  }

  implicit def classDescribable(
    t: Class[?]
  )(implicit formats: Formats = DefaultFormats): ReflectorDescribable[Class[?]] = new ReflectorDescribable[Class[?]] {
    val companionClasses: List[(Class[?], AnyRef)] = formats.companions
    val paranamer: ParameterNameReader = formats.parameterNameReader
    val scalaType: ScalaType = Reflector.scalaTypeOf(t)
  }

  implicit def stringDescribable(t: String)(implicit formats: Formats = DefaultFormats): ReflectorDescribable[String] =
    new ReflectorDescribable[String] {
      val companionClasses: List[(Class[?], AnyRef)] = formats.companions
      val paranamer: ParameterNameReader = formats.parameterNameReader
      val scalaType: ScalaType =
        Reflector.scalaTypeOf(t) getOrElse (throw new MappingException("Couldn't find class for " + t))
    }

  def fail(msg: String, cause: Exception = null) = throw new MappingException(msg, cause)
}
