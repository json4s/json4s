package org.json4s
package reflect

import java.lang.reflect.{AccessibleObject, Type, Constructor, Method}

import org.json4s.MappingException

/**
 * This class is intended as a workaround until we are able to use Java 8's java.lang.reflect.Executable class.
 */
class Executable private (val method: Method, val constructor: Constructor[?], isPrimaryCtor: Boolean) {

  def this(method: Method) = {
    this(method, null, false)
  }

  def this(constructor: Constructor[?], isPrimaryCtor: Boolean) = {
    this(null, constructor, isPrimaryCtor)
  }

  def defaultValuePattern: Option[String] = if (isPrimaryCtor) Some(ConstructorDefaultValuePattern) else None

  def getModifiers(): Int = {
    if (method != null) {
      method.getModifiers
    } else constructor.getModifiers
  }

  def getGenericParameterTypes(): scala.Array[Type] = {
    if (method != null) {
      method.getGenericParameterTypes
    } else constructor.getGenericParameterTypes
  }

  def getParameterTypes(): scala.Array[Class[?]] = {
    if (method != null) {
      method.getParameterTypes
    } else constructor.getParameterTypes
  }

  def getDeclaringClass(): Class[?] = {
    if (method != null) {
      method.getDeclaringClass
    } else constructor.getDeclaringClass
  }

  def invoke(companion: Option[SingletonDescriptor], args: Seq[Any]): Any = {
    if (method != null) {
      companion match {
        case Some(cmp) => method.invoke(cmp.instance, args.map(_.asInstanceOf[AnyRef]).toArray*)
        case None => throw new MappingException("Trying to call apply method, but the companion object was not found.")
      }
    } else {
      constructor.newInstance(args.map(_.asInstanceOf[AnyRef]).toArray*)
    }
  }

  def getAsAccessibleObject: AccessibleObject = {
    if (method != null)
      method
    else constructor
  }

  def getMarkedAsPrimary(): Boolean = {
    val markedByAnnotation = if (method == null) {
      constructor.isAnnotationPresent(classOf[PrimaryConstructor])
    } else {
      false
    }

    markedByAnnotation || isPrimaryCtor
  }

  override def toString =
    if (method != null)
      s"Executable(Method($method))"
    else s"Executable(Constructor($constructor))"
}
