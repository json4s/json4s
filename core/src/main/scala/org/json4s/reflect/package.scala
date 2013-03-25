package org.json4s

import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}
import java.lang.reflect.{Constructor, ParameterizedType, Type}
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import java.util.Date
import java.sql.Timestamp

package object reflect {

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
    def lookupParameterNames(constructor: Constructor[_]): Seq[String]
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
    def lookupParameterNames(constructor: Constructor[_]): Seq[String] =
      paranamer.lookupParameterNames(constructor).toSeq
  }

//  def isPrimitive(t: Type) = Reflector.isPrimitive(t)
//
//  def scalaTypeOf[T](implicit mf: Manifest[T]): ScalaType = types(mf.erasure, _ => Reflector.scalaTypeOf[T])
//
//  def scalaTypeOf(t: Type): ScalaType = types(t, Reflector.scalaTypeOf(_))
//
//  def describe[T](implicit mf: Manifest[T]): Descriptor = describe(scalaTypeOf[T])
//
//  def describe(clazz: Class[_]): Descriptor = describe(scalaTypeOf(clazz))
//
//  def describe(fqn: String): Option[Descriptor] = {
//    Reflector.scalaTypeOf(fqn) map { st =>
//      descriptors(st, Reflector.createClassDescriptor)
//    }
//  }
//
//  def describe(st: ScalaType): Descriptor = descriptors(st, Reflector.createClassDescriptor)
//
//  def rawClassOf(t: Type): Class[_] = rawClasses(t, Reflector.rawClassOf)
//
//  def unmangleName(name: String) = unmangledNames(name, Reflector.unmangleName)

  def fail(msg: String, cause: Exception = null) = throw new MappingException(msg, cause)
}
