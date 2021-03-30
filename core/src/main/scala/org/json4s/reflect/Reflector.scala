package org.json4s
package reflect

import java.lang.reflect.{Method, Modifier, ParameterizedType, Type, TypeVariable, WildcardType}
import scala.util.control.Exception._
import java.util.Date
import java.sql.Timestamp
import collection.mutable

object Reflector {

  private[this] val rawClasses = new Memo[Type, Class[_]]
  private[this] val unmangledNames = new Memo[String, String]
  private[this] val descriptors = new Memo[ScalaType, ObjectDescriptor]

  private[this] val primitives = {
    Set[Type](
      classOf[String],
      classOf[Int],
      classOf[Long],
      classOf[Double],
      classOf[Float],
      classOf[Byte],
      classOf[BigInt],
      classOf[Boolean],
      classOf[Short],
      classOf[java.lang.Integer],
      classOf[java.lang.Long],
      classOf[java.lang.Double],
      classOf[java.lang.Float],
      classOf[BigDecimal],
      classOf[java.lang.Byte],
      classOf[java.lang.Boolean],
      classOf[Number],
      classOf[java.lang.Short],
      classOf[Date],
      classOf[Timestamp],
      classOf[Symbol],
      classOf[java.math.BigDecimal],
      classOf[java.math.BigInteger]
    )
  }

  def clearCaches(): Unit = {
    rawClasses.clear()
    unmangledNames.clear()
    descriptors.clear()
    stringTypes.clear()
  }

  def isPrimitive(t: Type, extra: Set[Type] = Set.empty): Boolean =
    primitives(t) || extra(t)

  def scalaTypeOf[T](implicit mf: Manifest[T]): ScalaType = ScalaType(mf)
  def scalaTypeOf(clazz: Class[_]): ScalaType = ScalaType(ManifestFactory.manifestOf(clazz))
  def scalaTypeOf(t: Type): ScalaType = ScalaType(ManifestFactory.manifestOf(t))

  private[this] val stringTypes = new Memo[String, Option[ScalaType]]
  def scalaTypeOf(name: String): Option[ScalaType] =
    stringTypes(name, ScalaSigReader.resolveClass[AnyRef](_, ClassLoaders) map (c => scalaTypeOf(c)))

  def describe[T](implicit mf: Manifest[T], formats: Formats = DefaultFormats): ObjectDescriptor =
    describeWithFormats(scalaTypeDescribable(scalaTypeOf[T]))

  @deprecated("Use describe[T] or describeWithFormats instead", "3.6.8")
  def describe(st: ReflectorDescribable[_]): ObjectDescriptor =
    descriptors(st.scalaType, createDescriptorWithFormats(_, st.paranamer, st.companionClasses)(DefaultFormats))

  def describeWithFormats(st: ReflectorDescribable[_])(implicit formats: Formats): ObjectDescriptor =
    descriptors(st.scalaType, createDescriptorWithFormats(_, st.paranamer, st.companionClasses))

  @deprecated("Use createDescriptorWithFormats", "3.6.8")
  def createDescriptor(
    tpe: ScalaType,
    paramNameReader: ParameterNameReader = ParanamerReader,
    companionMappings: List[(Class[_], AnyRef)] = Nil
  ): ObjectDescriptor = {
    createDescriptorWithFormats(tpe, paramNameReader, companionMappings)(DefaultFormats)
  }

  def createDescriptorWithFormats(
    tpe: ScalaType,
    paramNameReader: ParameterNameReader = ParanamerReader,
    companionMappings: List[(Class[_], AnyRef)] = Nil
  )(implicit formats: Formats): ObjectDescriptor = {
    if (tpe.isPrimitive) PrimitiveDescriptor(tpe)
    else new ClassDescriptorBuilder(tpe, paramNameReader, companionMappings).result
  }

  private class ClassDescriptorBuilder(
    tpe: ScalaType,
    paramNameReader: ParameterNameReader = ParanamerReader,
    companionMappings: List[(Class[_], AnyRef)] = Nil
  )(implicit formats: Formats) {
    var companion: Option[SingletonDescriptor] = None
    var triedCompanion = false

    def fields(clazz: Class[_]): List[PropertyDescriptor] = {
      val lb = new mutable.ListBuffer[PropertyDescriptor]()
      val ls = allCatch.withApply(_ => fail("Case classes defined in function bodies are not supported.")) {
        clazz.getDeclaredFields.iterator
      }
      while (ls.hasNext) {
        val f = ls.next()
        val mod = f.getModifiers
        if (!(Modifier.isStatic(mod) || Modifier.isTransient(mod) || Modifier.isVolatile(mod) || f.isSynthetic)) {
          val st = ScalaType(
            f.getType,
            f.getGenericType match {
              case p: ParameterizedType =>
                p.getActualTypeArguments.toSeq.zipWithIndex map { case (cc, i) =>
                  if (cc == classOf[java.lang.Object])
                    Reflector.scalaTypeOf(ScalaSigReader.readField(f.getName, clazz, i))
                  else Reflector.scalaTypeOf(cc)
                }
              case _ => Nil
            }
          )
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

    def ctorParamType(
      name: String,
      index: Int,
      owner: ScalaType,
      ctorParameterNames: List[String],
      t: Type,
      container: Option[(ScalaType, List[Int])] = None
    ): ScalaType = {
      val idxes = container.map(_._2.reverse)
      t match {
        case v: TypeVariable[_] =>
          val a = owner.typeVars.getOrElse(v.getName, scalaTypeOf(v))
          if (a.erasure == classOf[java.lang.Object]) {
            val r = ScalaSigReader.readConstructor(name, owner, index, ctorParameterNames)
            scalaTypeOf(r)
          } else a
        case v: ParameterizedType =>
          val st = scalaTypeOf(v)
          val actualArgs = v.getActualTypeArguments.toList.zipWithIndex map { case (ct, idx) =>
            val prev = container.map(_._2).getOrElse(Nil)
            ctorParamType(name, index, owner, ctorParameterNames, ct, Some((st, idx :: prev)))
          }
          st.copy(typeArgs = actualArgs)
        case v: WildcardType =>
          val upper = v.getUpperBounds
          if (upper != null && upper.length > 0) scalaTypeOf(upper(0))
          else scalaTypeOf(Manifest.AnyRef)
        case x =>
          val st = scalaTypeOf(x)
          if (st.erasure == classOf[java.lang.Object]) {
            scalaTypeOf(ScalaSigReader.readConstructor(name, owner, idxes getOrElse List(index), ctorParameterNames))
          } else st
      }
    }

    def constructorsAndCompanion: Seq[ConstructorDescriptor] = {
      val er = tpe.erasure
      val ccs: Iterable[Executable] =
        allCatch.withApply(e => fail(e.getMessage + " Case classes defined in function bodies are not supported.")) {
          val ctors = er.getConstructors
          val primaryCtor = ctors.find(new Executable(_, false).getMarkedAsPrimary())
          primaryCtor match {
            case Some(ctor) => ctors.map(c => new Executable(c, c eq ctor))
            case None if ctors.length == 1 => ctors.map(c => new Executable(c, true))
            case None => ctors.map(new Executable(_, false))
          }
        }

      val constructorDescriptors = createConstructorDescriptors(ccs)
      val all = {
        if (constructorDescriptors.isEmpty || formats.considerCompanionConstructors) {
          companion = findCompanion(checkCompanionMapping = false)
          val applyMethods: scala.Array[Method] = companion match {
            case Some(singletonDescriptor) =>
              singletonDescriptor.instance.getClass.getMethods.filter { method =>
                method.getName == "apply" && method.getReturnType == er
              }
            case None => scala.Array[Method]()
          }
          val applyExecutables = applyMethods.map { m => new Executable(m) }
          constructorDescriptors ++ createConstructorDescriptors(applyExecutables)
        } else constructorDescriptors
      }
      // https://github.com/json4s/json4s/issues/371 no actual requirements for the order are known, but
      // deterministic ordering regardless of non-determinism by java.reflect is needed
      all.toList.sortBy(c => (!c.isPrimary, c.constructor.constructor == null, -c.params.size, c.toString))
    }

    def createConstructorDescriptors(ccs: Iterable[Executable]): Seq[ConstructorDescriptor] = {
      Option(ccs).map(_.toSeq).getOrElse(Nil) map { ctor =>
        val ctorParameterNames =
          if (Modifier.isPublic(ctor.getModifiers()) && ctor.getParameterTypes().length > 0)
            allCatch opt { paramNameReader.lookupParameterNames(ctor) } getOrElse Nil
          else
            Nil
        val genParams: Seq[Type] = {
          val types = ctor.getGenericParameterTypes()
          val diff = ctorParameterNames.length - types.length
          if (diff == 0) {
            types.toVector
          } else if (diff > 0) {
            // getGenericParameterTypes changed since Scala 2.13.0-RC2
            // https://github.com/scala/bug/issues/11542
            // https://github.com/scala/scala/commit/659dba63c37263e4d28bc9d4c9672d5af54d23ea
            Vector.fill(diff)(null) ++ types.toVector
          } else {
            // maybe impossible
            Vector.empty[Type]
          }
        }
        val ctorParams = ctorParameterNames.zipWithIndex map {
          case (ScalaSigReader.OuterFieldName, index) =>
            //            println("The result type of the $outer param: " + genParams(0))
            if (tpe.erasure.getDeclaringClass == null) fail("Classes defined in method bodies are not supported.")
            companion = findCompanion(checkCompanionMapping = true)
            val default = companionMappings.find(_._1 == tpe.erasure).map(_._2).map(() => _)
            val tt = scalaTypeOf(tpe.erasure.getDeclaringClass)
            ConstructorParamDescriptor(ScalaSigReader.OuterFieldName, ScalaSigReader.OuterFieldName, index, tt, default)
          case (paramName, index) =>
            companion = findCompanion(checkCompanionMapping = false)
            val decoded = unmangleName(paramName)
            val default = (companion, ctor.defaultValuePattern) match {
              case (Some(comp), Some(pattern)) =>
                defaultValue(comp.erasure.erasure, comp.instance, index, pattern)
              case _ => None
            }
            //println(s"$paramName $index $tpe $ctorParameterNames ${genParams(index)}")
            val theType = ctorParamType(
              paramName,
              index,
              tpe,
              ctorParameterNames.filterNot(_ == ScalaSigReader.OuterFieldName).toList,
              genParams(index)
            )
            ConstructorParamDescriptor(decoded, paramName, index, theType, default)
        }
        ConstructorDescriptor(ctorParams, ctor, isPrimary = ctor.getMarkedAsPrimary())
      }
    }

    def findCompanion(checkCompanionMapping: Boolean): Option[SingletonDescriptor] = {
      if (checkCompanionMapping) {
        val mapping = companionMappings.find(_._1 == tpe.erasure).map(_._2)
        mapping map { m =>
          val inst = m.getClass.getMethod(tpe.simpleName).invoke(m)
          val kl = inst.getClass
          SingletonDescriptor(safeSimpleName(kl), kl.getName, scalaTypeOf(kl), inst, Seq.empty)
        }
      } else {
        if (companion.isEmpty && !triedCompanion) {
          triedCompanion = true
          ScalaSigReader.companions(tpe.rawFullName) collect { case (kl, Some(cOpt)) =>
            SingletonDescriptor(safeSimpleName(kl), kl.getName, scalaTypeOf(kl), cOpt, Seq.empty)
          }
        } else companion
      }
    }

    def result: ClassDescriptor = {
      val constructors = constructorsAndCompanion
      ClassDescriptor(tpe.simpleName, tpe.fullName, tpe, companion, constructors, properties)
    }
  }

  def defaultValue(compClass: Class[_], compObj: AnyRef, argIndex: Int, pattern: String) = {
    allCatch.withApply(_ => None) {
      if (compObj == null) None
      else {
        Option(compClass.getMethod(pattern.format(argIndex + 1))) map { meth => () =>
          meth.invoke(compObj)
        }
      }
    }
  }

  def rawClassOf(t: Type): Class[_] = rawClasses(
    t,
    {
      case c: Class[_] => c
      case p: ParameterizedType => rawClassOf(p.getRawType)
      case x => sys.error(s"Raw type of $x not known")
    }
  )

  def unmangleName(name: String): String = unmangledNames(name, scala.reflect.NameTransformer.decode)

  def mkParameterizedType(owner: Type, typeArgs: Seq[Type]): ParameterizedType =
    new ParameterizedType {
      def getActualTypeArguments = typeArgs.toArray
      def getOwnerType = owner
      def getRawType = rawClassOf(owner)
      override def toString = getOwnerType().toString + "[" + getActualTypeArguments().mkString(",") + "]"
    }

}
