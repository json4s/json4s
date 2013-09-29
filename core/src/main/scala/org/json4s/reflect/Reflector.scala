package org.json4s
package reflect

import java.{util => jutil}
import java.lang.reflect._
import scala.util.control.Exception._
import scala.collection.JavaConverters._
import java.util.Date
import java.sql.Timestamp
import collection.mutable.ArrayBuffer
import annotation.tailrec
import collection.mutable

object Reflector {

  private[this] val rawClasses = new Memo[Type, Class[_]]
  private[this] val unmangledNames = new Memo[String, String]
  private[this] val descriptors = new Memo[ScalaType, ObjectDescriptor]

  private[this] val primitives = {
      Set[Type](classOf[String], classOf[Int], classOf[Long], classOf[Double],
        classOf[Float], classOf[Byte], classOf[BigInt], classOf[Boolean],
        classOf[Short], classOf[java.lang.Integer], classOf[java.lang.Long],
        classOf[java.lang.Double], classOf[java.lang.Float], classOf[BigDecimal],
        classOf[java.lang.Byte], classOf[java.lang.Boolean], classOf[Number],
        classOf[java.lang.Short], classOf[Date], classOf[Timestamp], classOf[Symbol],
        classOf[java.math.BigDecimal], classOf[java.math.BigInteger])
  }

  def isPrimitive(t: Type, extra: Set[Type] = Set.empty) = (primitives ++ extra) contains t

  def scalaTypeOf[T](implicit mf: Manifest[T]): ScalaType = ScalaType(mf)
  def scalaTypeOf(clazz: Class[_]): ScalaType = ScalaType(ManifestFactory.manifestOf(clazz))
  def scalaTypeOf(t: Type): ScalaType = ScalaType(ManifestFactory.manifestOf(t))

  private[this] val stringTypes = new Memo[String, Option[ScalaType]]
  def scalaTypeOf(name: String): Option[ScalaType] =
    stringTypes(name, ScalaSigReader.resolveClass[AnyRef](_, ClassLoaders) map (c => scalaTypeOf(c)))

  def describe[T](implicit mf: Manifest[T], formats: Formats = DefaultFormats): ObjectDescriptor =
    describe(scalaTypeDescribable(scalaTypeOf[T])(formats))

  def describe(st: ReflectorDescribable[_]): ObjectDescriptor =
    descriptors(st.scalaType, createDescriptor(_, st.paranamer, st.companionClasses))


  def createDescriptor(tpe: ScalaType, paramNameReader: ParameterNameReader = ParanamerReader, companionMappings: List[(Class[_], AnyRef)] = Nil): ObjectDescriptor = {
//    println("Creating descriptor for " + tpe.fullName + " and paramNameReader: " + paramNameReader + " and mappings: " + companionMappings)
    if (tpe.isPrimitive) PrimitiveDescriptor(tpe)
    else new ClassDescriptorBuilder(tpe, paramNameReader, companionMappings).result
  }

  private class ClassDescriptorBuilder(tpe: ScalaType, paramNameReader: ParameterNameReader = ParanamerReader, companionMappings: List[(Class[_], AnyRef)] = Nil) {
    var companion: Option[SingletonDescriptor] = None
    var triedCompanion = false

    def fields(clazz: Class[_]): List[PropertyDescriptor] = {
      val lb = new mutable.MutableList[PropertyDescriptor]()
      val ls = allCatch.withApply(_ => fail("Case classes defined in function bodies are not supported.")) { clazz.getDeclaredFields.toIterator }
      while (ls.hasNext) {
        val f = ls.next()
        if (!Modifier.isStatic(f.getModifiers) || !Modifier.isTransient(f.getModifiers) || !Modifier.isPrivate(f.getModifiers)) {
          val st = ScalaType(f.getType, f.getGenericType match {
            case p: ParameterizedType => p.getActualTypeArguments.toSeq.zipWithIndex map { case (cc, i) =>
              if (cc == classOf[java.lang.Object]) Reflector.scalaTypeOf(ScalaSigReader.readField(f.getName, clazz, i))
              else Reflector.scalaTypeOf(cc)
            }
            case _ => Nil
          })
          if (f.getName != ScalaSigReader.OuterFieldName) {
            val decoded = unmangleName(f.getName)
            f.setAccessible(true)
            lb += PropertyDescriptor(decoded, f.getName, st, f)
          }
        }
      }
      if (clazz.getSuperclass != null) lb ++= fields(clazz.getSuperclass)
      lb.toList
    }

    def properties: Seq[PropertyDescriptor] = fields(tpe.erasure)

    def ctorParamType(name: String, index: Int, owner: ScalaType, ctorParameterNames: List[String], t: Type, container: Option[(ScalaType, List[Int])] = None): ScalaType = {
      val idxes = container.map(_._2.reverse)
      t  match {
        case v: TypeVariable[_] =>
          val a = owner.typeVars.getOrElse(v, scalaTypeOf(v))
          if (a.erasure == classOf[java.lang.Object]) {
            val r = ScalaSigReader.readConstructor(name, owner, index, ctorParameterNames)
            scalaTypeOf(r)
          } else a
        case v: ParameterizedType =>
          val st = scalaTypeOf(v)
          val actualArgs = v.getActualTypeArguments.toList.zipWithIndex map {
            case (ct, idx) =>
              val prev = container.map(_._2).getOrElse(Nil)
              ctorParamType(name, index, owner, ctorParameterNames, ct, Some((st, idx :: prev)))
          }
          st.copy(typeArgs = actualArgs)
        case v: WildcardType =>
          val upper = v.getUpperBounds
          if (upper != null && upper.size > 0) scalaTypeOf(upper(0))
          else scalaTypeOf[AnyRef]
        case x =>
          val st = scalaTypeOf(x)
          if (st.erasure == classOf[java.lang.Object]) {
            scalaTypeOf(ScalaSigReader.readConstructor(name, owner, idxes getOrElse List(index), ctorParameterNames))
          } else st
      }
    }

    def constructorsAndCompanion: Seq[ConstructorDescriptor] = {
      val er = tpe.erasure
      val ccs = allCatch.withApply(_ => fail("Case classes defined in function bodies are not supported.")) { er.getConstructors }
      Option(ccs).map(_.toSeq).getOrElse(Nil) map { ctor =>
        val ctorParameterNames = if (Modifier.isPublic(ctor.getModifiers) && ctor.getParameterTypes.length > 0)
          allCatch opt { paramNameReader.lookupParameterNames(ctor) } getOrElse Nil
        else
          Nil
        val genParams = Vector(ctor.getGenericParameterTypes: _*)
        val ctorParams = ctorParameterNames.zipWithIndex map {
          case (ScalaSigReader.OuterFieldName, index) =>
//            println("The result type of the $outer param: " + genParams(0))
            if (tpe.erasure.getDeclaringClass == null) fail("Classes defined in method bodies are not supported.")
            val mapping = companionMappings.find(_._1 == tpe.erasure).map(_._2)
            companion = mapping map { o =>
              val inst = o.getClass.getMethod(tpe.simpleName).invoke(o)
              val kl = inst.getClass
              SingletonDescriptor(safeSimpleName(kl), kl.getName, scalaTypeOf(kl), inst, Seq.empty)
            }

            val default = mapping.map(() => _)
            val tt = scalaTypeOf(tpe.erasure.getDeclaringClass)
            ConstructorParamDescriptor(ScalaSigReader.OuterFieldName, ScalaSigReader.OuterFieldName, index, tt, default)
          case (paramName, index) =>
            if (companion.isEmpty && !triedCompanion) {
              companion = (ScalaSigReader.companions(tpe.rawFullName) collect {
                case (kl, Some(cOpt)) => SingletonDescriptor(safeSimpleName(kl),
                                                             kl.getName, scalaTypeOf(kl), cOpt, Seq.empty)
              })
              triedCompanion = true
            }
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

    def result = {
      val constructors = constructorsAndCompanion
//      println("companion: " + companion)
      ClassDescriptor(tpe.simpleName, tpe.fullName, tpe, companion, constructors, properties)
    }
  }

  def defaultValue(compClass: Class[_], compObj: AnyRef, argIndex: Int) = {
    allCatch.withApply(_ => None) {
      if (compObj == null) None
      else {
        Option(compClass.getMethod("%s$%d".format(ConstructorDefault, argIndex + 1))) map {
          meth => () => meth.invoke(compObj)
        }
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
