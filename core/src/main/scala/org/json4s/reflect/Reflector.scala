package org.json4s
package reflect

import java.{util => jutil}
import java.lang.reflect._
import scala.util.control.Exception._
import scalaj.collection.Imports._
import java.util.Date
import java.sql.Timestamp
import scalashim._
import collection.mutable.ArrayBuffer
import annotation.tailrec
import reflect.SingletonDescriptor
import reflect.ClassDescriptor
import scala.Some
import reflect.ConstructorParamDescriptor
import reflect.PropertyDescriptor
import reflect.ConstructorDescriptor

object Reflector {

  private[this] val rawClasses = new Memo[Type, Class[_]]
  private[this] val unmangledNames = new Memo[String, String]
  private[this] val types = new Memo[Type, ScalaType]
  private[this] val descriptors = new Memo[ScalaType, Descriptor]

  private[this] val primitives = {
      Set[Type](classOf[String], classOf[Int], classOf[Long], classOf[Double],
        classOf[Float], classOf[Byte], classOf[BigInt], classOf[Boolean],
        classOf[Short], classOf[java.lang.Integer], classOf[java.lang.Long],
        classOf[java.lang.Double], classOf[java.lang.Float],
        classOf[java.lang.Byte], classOf[java.lang.Boolean], classOf[Number],
        classOf[java.lang.Short], classOf[Date], classOf[Timestamp], classOf[Symbol])
  }

  def isPrimitive(t: Type) = primitives contains t

  def scalaTypeOf[T](implicit mf: Manifest[T]): ScalaType = {
//    types(mf.erasure, _ => ScalaType(mf))
    ScalaType(mf)
  }

  def scalaTypeOf(clazz: Class[_]): ScalaType = {
    val mf = ManifestFactory.manifestOf(clazz)
//    types(mf.erasure, _ => ScalaType(mf))
    ScalaType(mf)
  }

  def scalaTypeOf(t: Type): ScalaType = {
    val mf = ManifestFactory.manifestOf(t)
//    types(mf.erasure, _ => ScalaType(mf))
    ScalaType(mf)
  }

  def scalaTypeOf(name: String): Option[ScalaType] =
    Reflector.resolveClass[AnyRef](name, ClassLoaders) map (c => scalaTypeOf(c))

  def describe[T](implicit mf: Manifest[T]): Descriptor = describe(scalaTypeOf[T])

  def describe(clazz: Class[_]): Descriptor = describe(scalaTypeOf(clazz))

  def describe(fqn: String): Option[Descriptor] = {
    Reflector.scalaTypeOf(fqn) map { st => descriptors(st, createClassDescriptor) }
  }

  def describe(st: ScalaType): Descriptor = descriptors(st, Reflector.createClassDescriptor)


  def resolveClass[X <: AnyRef](c: String, classLoaders: Iterable[ClassLoader]): Option[Class[X]] = classLoaders match {
    case Nil => sys.error("resolveClass: expected 1+ classloaders but received empty list")
    case List(cl) => Some(Class.forName(c, true, cl).asInstanceOf[Class[X]])
    case many => {
      try {
        var clazz: Class[_] = null
        val iter = many.iterator
        while (clazz == null && iter.hasNext) {
          try {
            clazz = Class.forName(c, true, iter.next())
          }
          catch {
            case e: ClassNotFoundException => // keep going, maybe it's in the next one
          }
        }

        if (clazz != null) Some(clazz.asInstanceOf[Class[X]]) else None
      }
      catch {
        case _: Throwable => None
      }
    }
  }

  private[reflect] def createClassDescriptor(tpe: ScalaType): Descriptor = {
    val path = if (tpe.rawFullName.endsWith("$")) tpe.rawFullName else "%s$".format(tpe.rawFullName)
    val c = resolveClass(path, Vector(getClass.getClassLoader))
    val companion = c flatMap { cl =>
        allCatch opt {
          SingletonDescriptor(cl.getSimpleName, cl.getName, scalaTypeOf(cl), cl.getField(ModuleFieldName).get(null), Seq.empty)
        }
    }

    def properties: Seq[PropertyDescriptor] = {
      def fields(clazz: Class[_]): List[PropertyDescriptor] = {
        val lb = new ArrayBuffer[PropertyDescriptor]()
        val ls = clazz.getDeclaredFields.toIterator
        while (ls.hasNext) {
          val f = ls.next()
          if (!Modifier.isStatic(f.getModifiers) || !Modifier.isTransient(f.getModifiers) || !Modifier.isPrivate(f.getModifiers)) {
            val st = ScalaType(f.getType, f.getGenericType match {
              case p: ParameterizedType => p.getActualTypeArguments map (c => scalaTypeOf(c))
              case _ => Nil
            })
            val decoded = unmangleName(f.getName)
            f.setAccessible(true)
            lb += PropertyDescriptor(decoded, f.getName, st, f)
          }
        }
        if (clazz.getSuperclass != null)
          lb ++= fields(clazz.getSuperclass)
        lb.toList
      }
      fields(tpe.erasure)
    }

    def ctorParamType(name: String, index: Int, owner: ScalaType, ctorParameterNames: List[String], t: Type, container: Option[(ScalaType, List[Int])] = None): ScalaType = {
//      println("Getting %s at %d on %s from type %s contained by %s".format(name, index, owner, t, container))
      val idxes = container.map(_._2.reverse)
      t  match {
        case v: TypeVariable[_] =>
//          println("This is a type variable " + v)
          val a = owner.typeVars.getOrElse(v, scalaTypeOf(v))
          if (a.erasure == classOf[java.lang.Object]) {
//            println("falling back to scalasig")
            val r = ScalaSigReader.readConstructor(name, owner, index, ctorParameterNames)
            scalaTypeOf(r)
          } else a
        case v: ParameterizedType =>
//          println("This is a parameterized type " + v)
          val st = scalaTypeOf(v)
          val actualArgs = v.getActualTypeArguments.toList.zipWithIndex map {
            case (ct, idx) =>
              val prev = container.map(_._2).getOrElse(Nil)
              ctorParamType(name, index, owner, ctorParameterNames, ct, Some((st, idx :: prev)))
          }
//          println("actualArgs: " + actualArgs)
          st.copy(typeArgs = actualArgs)
        case v: WildcardType =>
//          println("this is a wildcard type: " + t)
          val upper = v.getUpperBounds
          if (upper != null && upper.size > 0) scalaTypeOf(upper(0))
          else scalaTypeOf[AnyRef]
        case x =>
//          println("This is a plain type: " + x)
          val st = scalaTypeOf(x)
          if (st.erasure == classOf[java.lang.Object]) {
            println("falling back to scalasig")
            scalaTypeOf(ScalaSigReader.readConstructor(name, owner, idxes getOrElse List(index), ctorParameterNames))
          } else st
      }
    }

    def constructors: Seq[ConstructorDescriptor] = {
      tpe.erasure.getConstructors.toSeq map {
        ctor =>
          val ctorParameterNames = ParanamerReader.lookupParameterNames(ctor)
          val genParams = Vector(ctor.getGenericParameterTypes: _*)
          val ctorParams = ctorParameterNames.zipWithIndex map {
            case (paramName, index) =>
              val decoded = unmangleName(paramName)
              val default = companion flatMap {
                comp =>
                  defaultValue(comp.erasure.erasure, comp.instance, index)
              }
              val theType = ctorParamType(paramName, index, tpe, ctorParameterNames.toList, genParams(index))
              ConstructorParamDescriptor(decoded, paramName, index, theType, default)
          }
          ConstructorDescriptor(ctorParams.toSeq, ctor, isPrimary = false)
      }
    }

    ClassDescriptor(tpe.simpleName, tpe.fullName, tpe, companion, constructors, properties)
  }

  def defaultValue(compClass: Class[_], compObj: AnyRef, argIndex: Int) = {
    allCatch.withApply(_ => None) {
      Option(compClass.getMethod("%s$%d".format(ConstructorDefault, argIndex + 1))) map {
        meth => () => meth.invoke(compObj)
      }
    }
  }

  def rawClassOf(t: Type): Class[_] = rawClasses(t, _ match {
    case c: Class[_] => c
    case p: ParameterizedType => rawClassOf(p.getRawType)
    case x => sys.error("Raw type of " + x + " not known")
  })

  def unmangleName(name: String) = unmangledNames(name, scala.reflect.NameTransformer.decode)

  def mkParameterizedType(owner: Type, typeArgs: Seq[Type]) =
    new ParameterizedType {
      def getActualTypeArguments = typeArgs.toArray
      def getOwnerType = owner
      def getRawType = rawClassOf(owner)
      override def toString = getOwnerType + "[" + getActualTypeArguments.mkString(",") + "]"
    }

}