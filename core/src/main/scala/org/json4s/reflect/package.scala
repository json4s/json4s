package org.json4s

import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}
import java.lang.reflect._
import java.util.concurrent.ConcurrentHashMap

package object reflect {

  def safeSimpleName(clazz: Class[_]) =
    try {
      clazz.getSimpleName
    } catch {
      case t: Throwable =>
        val packageNameLen = Some(clazz.getPackage) map (_.getName.length + 1) getOrElse 0
        stripDollar(clazz.getName.substring(packageNameLen))
    }

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

  private[reflect] class Memo[A, R] {
    private[this] val cache = new ConcurrentHashMap[A, R](1500, 1, 1)

    def apply(x: A, f: A => R): R = {
      if (cache.containsKey(x))
        cache.get(x)
      else {
        val v = f(x)
        replace(x, v)
      }
    }

    def replace(x: A, v: R):R = {
      cache.put(x, v)
      v
    }

    def clear() = cache.clear()
  }

  private[reflect] val ConstructorDefault = "$lessinit$greater$default"
  private[reflect] val ModuleFieldName = "MODULE$"
  private[reflect] val ClassLoaders = Vector(getClass.getClassLoader)
  private[this] val paranamer = new CachingParanamer(new BytecodeReadingParanamer)



  case class TypeInfo(clazz: Class[_], parameterizedType: Option[ParameterizedType])

  private[reflect] trait SourceType {
    def scalaType: ScalaType
  }

  trait ParameterNameReader {
    def lookupParameterNames(constructor: reflect.Executable): Seq[String]
  }

  trait ReflectorDescribable[T] {
    def companionClasses: List[(Class[_], AnyRef)]
    def paranamer: ParameterNameReader
    def scalaType: ScalaType
  }

  implicit def scalaTypeDescribable(t: ScalaType)(implicit formats: Formats = DefaultFormats): ReflectorDescribable[ScalaType] = new ReflectorDescribable[ScalaType] {
    val companionClasses: List[(Class[_], AnyRef)] = formats.companions
    val paranamer: ParameterNameReader = formats.parameterNameReader
    val scalaType: ScalaType = t
  }

  implicit def classDescribable(t: Class[_])(implicit formats: Formats = DefaultFormats): ReflectorDescribable[Class[_]] = new ReflectorDescribable[Class[_]] {
    val companionClasses: List[(Class[_], AnyRef)] = formats.companions
    val paranamer: ParameterNameReader = formats.parameterNameReader
    val scalaType: ScalaType =  Reflector.scalaTypeOf(t)
  }

  implicit def stringDescribable(t: String)(implicit formats: Formats = DefaultFormats): ReflectorDescribable[String] = new ReflectorDescribable[String] {
    val companionClasses: List[(Class[_], AnyRef)] = formats.companions
    val paranamer: ParameterNameReader = formats.parameterNameReader
    val scalaType: ScalaType = Reflector.scalaTypeOf(t) getOrElse (throw new MappingException("Couldn't find class for " + t))
  }

  object ParanamerReader extends ParameterNameReader {
    def lookupParameterNames(constructor: reflect.Executable): Seq[String] =
      paranamer.lookupParameterNames(constructor.getAsAccessibleObject).toSeq
  }

  def fail(msg: String, cause: Exception = null) = throw new MappingException(msg, cause)
}
