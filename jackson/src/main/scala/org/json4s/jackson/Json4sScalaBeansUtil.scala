package org.json4s
package jackson

import org.scalastuff.scalabeans.types.ScalaType
import org.scalastuff.scalabeans.sig.{Mirror, ClassDeclExtractor}
import org.scalastuff.scalabeans._
import org.scalastuff.scalabeans.Preamble._
import java.lang.reflect._
import com.thoughtworks.paranamer.BytecodeReadingParanamer
import collection.JavaConversions._
import Preamble._
import org.scalastuff.scalabeans.types.ScalaType


private abstract class ScalaTypeImpl(val erasure: Class[_], val arguments: ScalaType*) extends ScalaType


object WorkingBeanIntrospector {
  def apply[T <: AnyRef](mf: Manifest[_]): BeanDescriptor = apply[T](Preamble.scalaTypeOf(mf))

  def apply[T <: AnyRef](_beanType: ScalaType) = {

    def getTopLevelClass(c: Class[_]): Class[_] = c.getSuperclass match {
      case null => c
      case superClass if superClass == classOf[java.lang.Object] => c
      case superClass => getTopLevelClass(superClass)
    }

    val c = _beanType.erasure

    def classExtent(c: Class[_]): List[Class[_]] = {
      if (c == classOf[AnyRef]) Nil
      else classExtent(c.getSuperclass) :+ c
    }

    /**
     * Constructor. Secondary constructors are not supported
     */
    val constructor: Option[Constructor[_]] = {
      if (c.getConstructors().isEmpty) None
      else Some(c.getConstructors()(0).asInstanceOf[Constructor[_]])
    }

    val paranamer = new BytecodeReadingParanamer
    val ctorParameterNames = constructor.map(paranamer.lookupParameterNames(_)).getOrElse(scala.Array[String]())

    var tag = 0
    var mutablePropertyPosition = 0
    def createPropertyDescriptor(beanType: ScalaType, name: String, field: Option[Field], getter: Option[Method], setter: Option[Method]) = {
      tag = tag + 1

      var ctorIndex = ctorParameterNames.indexOf(name)
      if (ctorIndex < 0) {
        ctorIndex = ctorParameterNames.indexOf("_" + name)

        // check if declared in superclass, otherwise does not allow _name
        if (ctorIndex >= 0) {
          val accessible = field orElse getter get

          if (accessible.getDeclaringClass == beanType.erasure)
            ctorIndex = -1
        }
      }

      val defaultValueMethod =
        if (ctorIndex < 0) None
        else beanType.erasure.getMethods.find(_.getName == "init$default$" + (ctorIndex + 1))

      val descriptor = PropertyDescriptor(beanType, tag, mutablePropertyPosition, field, getter, setter, ctorIndex, defaultValueMethod)
      if (descriptor.isInstanceOf[MutablePropertyDescriptor] && !descriptor.isInstanceOf[ConstructorParameter])
        mutablePropertyPosition += 1

      descriptor
    }

    //
    // Properties of the class.
    //

    /**
     * Searches for the method with given name in the given class. Overridden method discovered if present.
     */
    def findMethod(c: Class[_], name: String): Option[Method] = {
      if (c == null) None
      else if (c == classOf[AnyRef]) None
      else c.getDeclaredMethods.find(_.getName == name) orElse findMethod(c.getSuperclass(), name)
    }

    def typeSupported(scalaType: ScalaType) = {
      true // TODO: list of supported Java and Scala types ...
    }

    val fieldProperties = for {
      c <- classExtent(c)
      field <- c.getDeclaredFields
      name = field.getName

      if ctorParameterNames.contains(name) || !name.contains('$')
      if !field.isSynthetic
  //      if typeSupported(scalaTypeOf(field.getGenericType))

      getter = findMethod(_beanType.erasure, name)
      setter = findMethod(_beanType.erasure, name + "_$eq")
    } yield createPropertyDescriptor(_beanType, name, Some(field), getter, setter)

    val methodProperties = for {
      c <- classExtent(c)
      getter <- c.getDeclaredMethods
      name = getter.getName

      if getter.getParameterTypes.length == 0
      if getter.getReturnType != Void.TYPE
      if !name.contains('$')
  //      if typeSupported(scalaTypeOf(getter.getGenericReturnType))
      if !fieldProperties.exists(_.name == name)
      setter <- c.getDeclaredMethods.find(_.getName == name + "_$eq")

    } yield createPropertyDescriptor(_beanType, name, None, Some(getter), Some(setter))

    new BeanDescriptor {
      val beanType = _beanType
      val properties = fieldProperties ++ methodProperties

      val topLevelClass = getTopLevelClass(c)
    }
  }

  def print(c: Class[_], prefix: String = "") : Unit = {
    def static(mods : Int) = if (Modifier.isStatic(mods)) " (static)" else ""
    println(prefix + "Class: " + c.getName)
    println(prefix + "  Fields: ")
    for (f <- c.getDeclaredFields) {
      println(prefix + "    " + f.getName + " : " + f.getGenericType + static(f.getModifiers))
      if (f.getName == "$outer") print(f.getType, "      ")
    }
    println(prefix + "  Methods: ")
    for (f <- c.getDeclaredMethods) {
      println(prefix + "    " + f.getName + " : " + f.getGenericReturnType + static(f.getModifiers))
    }
    println(prefix + "  Sub classes: ")
    for (f <- c.getDeclaredClasses) {
      println(prefix + "    " + f.getName + static(f.getModifiers))
    }
    println(prefix + "  Enum Values: ")
    for (f <- c.getMethods filter (m => m.getParameterTypes.isEmpty && classOf[Enumeration$Value].isAssignableFrom(m.getReturnType))) {
      val instance = new Enumeration{}
      println(prefix + "    a" )
    }
  }
}

object Json4sScalaBeansUtil {
  def propertiesOf(cls: Class[_]): Seq[PropertyDescriptor] = {
    val anyType = scalaTypeOf(classOf[Any])
    val scalaType = scalaTypeOf(cls)

    val typeWithFakeParamTypes = for {
      top <- ClassDeclExtractor.extract(scalaType.erasure)
      classDecl <- top.headOption
      if classDecl.isInstanceOf[Mirror.ClassDecl]
    } yield {
      val args = classDecl.asInstanceOf[Mirror.ClassDecl].typeParameters.map(_ => anyType)
      new ScalaTypeImpl(cls, args: _*) {}
    }

    val typ = typeWithFakeParamTypes.getOrElse(scalaType)
    WorkingBeanIntrospector[AnyRef](typ).properties
  }
}
