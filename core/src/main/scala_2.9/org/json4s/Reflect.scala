//package org.json4s
//
//import collection.JavaConverters._
//import java.lang.reflect._
//import java.util.concurrent.ConcurrentHashMap
//import java.util.Date
//import java.sql.Timestamp
//import java.{ util => jutil }
//import com.thoughtworks.paranamer.{CachingParanamer, BytecodeReadingParanamer}
//import reflect.ManifestFactory
//import util.control.Exception.allCatch
//import scala.tools.scalap.scalax.rules.scalasig.MethodSymbol
//
//object Reflect {
//  private[this] val ConstructorDefault = "init$default"
//  private[this] val ModuleFieldName = "MODULE$"
//  private[this] val ClassLoaders = Vector(getClass.getClassLoader)
//  private[this] val paranamer = new CachingParanamer(new BytecodeReadingParanamer)
//  private[this] val rawClasses = new Memo[Type, Class[_]]
//
//  object ParanamerReader extends ParameterNameReader {
//      def lookupParameterNames(constructor: Constructor[_]): Seq[String] =
//        paranamer.lookupParameterNames(constructor)
//    }
//  private[this] val unmangledNames = new Memo[String, String]
//
//  private[this] val primitives = Map[Class[_], Unit]() ++ (List[Class[_]](
//        classOf[String], classOf[Int], classOf[Long], classOf[Double],
//        classOf[Float], classOf[Byte], classOf[BigInt], classOf[Boolean],
//        classOf[Short], classOf[java.lang.Integer], classOf[java.lang.Long],
//        classOf[java.lang.Double], classOf[java.lang.Float],
//        classOf[java.lang.Byte], classOf[java.lang.Boolean], classOf[Number],
//        classOf[java.lang.Short], classOf[Date], classOf[Timestamp], classOf[Symbol]).map((_, ())))
//
//
//  def isPrimitive(t: Type) = t match {
//    case clazz: Class[_] => primitives contains clazz
//    case _ => false
//  }
//
//  sealed trait Descriptor
//  object ScalaType {
//    def apply[T](mf: Manifest[T]): ScalaType = {
//      new ScalaType(
//        mf.erasure,
//        mf.typeArguments.map(ta => scalaTypeOf(ta)),
//        Map.empty ++
//          mf.erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[_]]).toList.zip(mf.typeArguments map (ScalaType(_))))
//    }
//  }
//  case class ScalaType(erasure: Class[_], typeArgs: Seq[ScalaType], typeVars: Map[TypeVariable[_], ScalaType]) extends Descriptor {
//    lazy val rawFullName: String = erasure.getName
//    lazy val rawSimpleName: String = erasure.getSimpleName
//    lazy val simpleName: String = rawSimpleName + (if (typeArgs.nonEmpty) typeArgs.map(_.simpleName).mkString("[", ", ", "]") else "")
//    lazy val fullName: String = rawFullName + (if (typeArgs.nonEmpty) typeArgs.map(_.fullName).mkString("[", ", ", "]") else "")
//    lazy val typeInfo: TypeInfo = TypeInfo(erasure, if (typeArgs.nonEmpty) Some(Meta.mkParameterizedType(erasure, typeArgs.map(_.erasure).toSeq)) else None)
//  }
//  case class PropertyDescriptor(name: String, mangledName: String, returnType: ScalaType, field: Field) extends Descriptor {
//    def set(receiver: Any, value: Any) = field.set(receiver, value)
//    def get(receiver: AnyRef) = field.get(receiver)
//  }
//  case class ConstructorParamDescriptor(name: String, mangledName: String, argIndex: Int, argType: ScalaType, defaultValue: Option[() => Any]) extends Descriptor {
//    lazy val isOptional = defaultValue.isDefined || classOf[Option[_]].isAssignableFrom(argType.erasure)
//  }
//  case class ConstructorDescriptor(params: Seq[ConstructorParamDescriptor], constructor: java.lang.reflect.Constructor[_], isPrimary: Boolean) extends Descriptor
//  case class SingletonDescriptor(simpleName: String, fullName: String, erasure: ScalaType, instance: AnyRef, properties: Seq[PropertyDescriptor]) extends Descriptor
//  case class ClassDescriptor(simpleName: String, fullName: String, erasure: ScalaType, companion: Option[SingletonDescriptor], constructors: Seq[ConstructorDescriptor], properties: Seq[PropertyDescriptor]) extends Descriptor {
////    def bestConstructor(argNames: Seq[String]): Option[ConstructorDescriptor] = {
////      constructors.sortBy(-_.params.size)
////    }
//  }
//
//  private[this] val types = new Memo[Class[_], ScalaType]
//  private[this] val descriptors = new Memo[ScalaType, Descriptor]
//
//  def scalaTypeOf[T](implicit mf: Manifest[T]): ScalaType = {
//    types(
//      mf.erasure,
//      cl => {
//        ScalaType(mf)
//      })
//  }
//
//  def scalaTypeOf(clazz: Class[_]): ScalaType = {
//    types(
//      clazz,
//      cl => {
//        val mf = ManifestFactory.manifestOf(clazz)
//        scalaTypeOf(mf)
//      })
//  }
//
//  def scalaTypeOf(t: Type): ScalaType = {
//    val mf = ManifestFactory.manifestOf(t)
//    ScalaType(mf)
//  }
//
//  def scalaTypeOf(name: String): Option[ScalaType] = resolveClass[AnyRef](name, ClassLoaders) map (c => scalaTypeOf(c))
//
//  def describe[T](implicit mf: Manifest[T]): Descriptor = describe(scalaTypeOf[T])
//
//  def describe(clazz: Class[_]): Descriptor = describe(scalaTypeOf(clazz))
//
//  def describe(fqn: String): Option[Descriptor] = {
//    scalaTypeOf(fqn) map { st =>
//      descriptors(st, createClassDescriptor)
//    }
//  }
//
//  def describe(st: ScalaType): Descriptor = descriptors(st, createClassDescriptor)
//
//  def resolveClass[X <: AnyRef](c: String, classLoaders: Iterable[ClassLoader]): Option[Class[X]] = classLoaders match {
//    case Nil      => sys.error("resolveClass: expected 1+ classloaders but received empty list")
//    case List(cl) => Some(Class.forName(c, true, cl).asInstanceOf[Class[X]])
//    case many => {
//      try {
//        var clazz: Class[_] = null
//        val iter = many.iterator
//        while (clazz == null && iter.hasNext) {
//          try {
//            clazz = Class.forName(c, true, iter.next())
//          }
//          catch {
//            case e: ClassNotFoundException => // keep going, maybe it's in the next one
//          }
//        }
//
//        if (clazz != null) Some(clazz.asInstanceOf[Class[X]]) else None
//      }
//      catch {
//        case _: Throwable => None
//      }
//    }
//  }
//  private[this] def createClassDescriptor(tpe: ScalaType): Descriptor = {
//    val path = if (tpe.rawFullName.endsWith("$")) tpe.rawFullName else "%s$".format(tpe.rawFullName)
////    val sig = ScalaSigReader.findScalaSig(tpe.erasure).getOrElse(Meta.fail("Can't find ScalaSig for " + tpe.fullName))
////    val sym = ScalaSigReader.findClass(sig, tpe.erasure).getOrElse(Meta.fail("Can't find " + tpe.fullName + " from parsed ScalaSig"))
////    val children = sym.children
////    val ctorChildren =
////      children.filter(c => c.isCaseAccessor && !c.isPrivate).map(_.asInstanceOf[MethodSymbol]).zipWithIndex map {
////        case (ms, idx) =>
////          ConstructorParamDescriptor(unmangleName(ms.name), ms.name, idx, null, )
////      }
//    val c = resolveClass(path, Vector(getClass.getClassLoader))
//    val companion = c map { cl =>
//      SingletonDescriptor(cl.getSimpleName, cl.getName, scalaTypeOf(cl), cl.getField(ModuleFieldName).get(null), Seq.empty)
//    }
//
//    def properties: Seq[PropertyDescriptor] = {
//      def fields(clazz: Class[_]): List[PropertyDescriptor] = {
//        val lb = new jutil.LinkedList[PropertyDescriptor]().asScala
//        val ls = clazz.getDeclaredFields.toIterator
//        while(ls.hasNext) {
//          val f = ls.next()
//          if (!Modifier.isStatic(f.getModifiers) || !Modifier.isTransient(f.getModifiers) || !Modifier.isPrivate(f.getModifiers)) {
//            val st = ScalaType(f.getType, f.getGenericType match {
//              case p: ParameterizedType => p.getActualTypeArguments map (c => scalaTypeOf(c))
//              case _ => Nil
//            }, Map.empty)
//            val decoded = unmangleName(f.getName)
//            f.setAccessible(true)
//            lb += PropertyDescriptor(decoded, f.getName, st, f)
//          }
//        }
//        if (clazz.getSuperclass != null)
//          lb ++= fields(clazz.getSuperclass)
//        lb.toList
//      }
//      fields(tpe.erasure)
//    }
//
//    def constructors: Seq[ConstructorDescriptor] = {
//      tpe.erasure.getConstructors.toSeq map { ctor =>
//        val ctorParameterNames = ParanamerReader.lookupParameterNames(ctor)
//        val genParams = Vector(ctor.getGenericParameterTypes:_*)
//        val ctorParams = ctorParameterNames.zipWithIndex map { cp =>
//          val decoded = unmangleName(cp._1)
//          val default = companion flatMap { comp =>
//            defaultValue(comp.erasure.erasure, comp.instance, cp._2)
//          }
//          val theType = genParams(cp._2) match {
//            case v: TypeVariable[_] =>
//              val a = tpe.typeVars.getOrElse(v, scalaTypeOf(v))
//              if (a.erasure == classOf[java.lang.Object])
//                scalaTypeOf(ScalaSigReader.readConstructor(cp._1, tpe.erasure, cp._2, ctorParameterNames.toList))
//              else a
//            case x => scalaTypeOf(x)
//          }
//          ConstructorParamDescriptor(decoded, cp._1, cp._2, theType, default)
//        }
//        ConstructorDescriptor(ctorParams.toSeq, ctor, isPrimary = false)
//      }
//    }
//
//    ClassDescriptor(tpe.simpleName, tpe.fullName, tpe, companion, constructors, properties)
//  }
//
//  def defaultValue(compClass: Class[_], compObj: AnyRef, argIndex: Int) = {
//    allCatch.withApply(_ => None) {
//      Option(compClass.getMethod("%s$%d".format(ConstructorDefault, argIndex + 1))) map { meth => () => meth.invoke(compObj) }
//    }
//  }
//
//  private[this] def rawClassOf(t: Type): Class[_] = rawClasses(t, _ match {
//    case c: Class[_] => c
//    case p: ParameterizedType =>
//      rawClassOf(p.getRawType)
//    case x => sys.error("Raw type of " + x + " not known")
//  })
//
//  private[json4s] def unmangleName(name: String) =
//    unmangledNames(name, scala.reflect.NameTransformer.decode)
//
//
//  private class Memo[A, R] {
//    private[this] val cache = new ConcurrentHashMap[A, R]().asScala
//    def apply(x: A, f: A => R): R = cache.getOrElseUpdate(x, f(x))
//  }
//
//}