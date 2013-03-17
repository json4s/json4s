package org.json4s
package reflect

import java.lang.reflect.{Constructor => JConstructor, Type, Field, TypeVariable}
import scala._

sealed trait Descriptor
object ScalaType {
  def apply[T](mf: Manifest[T]): ScalaType = new ScalaType(mf)

  def apply(erasure: Class[_], typeArgs: Seq[ScalaType] = Seq.empty): ScalaType = {
    val mf = ManifestFactory.manifestOf(erasure, typeArgs.map(_.manifest))
    new ScalaType(mf)
  }

  def apply(target: TypeInfo): ScalaType = {
    target match {
      case t: TypeInfo with SourceType => t.scalaType
      case t =>
        val tArgs = t.parameterizedType.map(_.getActualTypeArguments.toList.map(Reflector.scalaTypeOf(_))).getOrElse(Nil)
        ScalaType(target.clazz, tArgs)
    }
  }
}
class ScalaType(private val manifest: Manifest[_]) extends Equals {

  private[this] val self = this
  val erasure: Class[_] = manifest.erasure

  val typeArgs: Seq[ScalaType] = manifest.typeArguments.map(ta => Reflector.scalaTypeOf(ta)) ++ (
    if (erasure.isArray) List(Reflector.scalaTypeOf(erasure.getComponentType)) else Nil
  )

  val typeVars: Map[TypeVariable[_], ScalaType] = Map.empty ++
    erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[_]]).toList.zip(manifest.typeArguments map (ScalaType(_)))

  val isArray: Boolean = erasure.isArray

  private[this] var _rawFullName: String = null
  def rawFullName: String = {
    if (_rawFullName == null)
      _rawFullName = erasure.getName
    _rawFullName
  }

  private[this] var _rawSimpleName: String = null
  def rawSimpleName: String = {
    if (_rawSimpleName == null)
      _rawSimpleName = erasure.getSimpleName
    _rawSimpleName
  }

  lazy val simpleName: String =
    rawSimpleName + (if (typeArgs.nonEmpty) typeArgs.map(_.simpleName).mkString("[", ", ", "]") else (if (typeVars.nonEmpty) typeVars.map(_._2.simpleName).mkString("[", ", ", "]") else ""))

  lazy val fullName: String =
    rawFullName + (if (typeArgs.nonEmpty) typeArgs.map(_.fullName).mkString("[", ", ", "]") else "")

  lazy val typeInfo: TypeInfo =
    new TypeInfo(
      erasure,
      if (typeArgs.nonEmpty) Some(Reflector.mkParameterizedType(erasure, typeArgs.map(_.erasure).toSeq)) else None
    ) with SourceType {
      val scalaType: ScalaType = self
    }

  lazy val isPrimitive = Reflector.isPrimitive(erasure)
  def isPrimitive(extra: Set[Type] = Set.empty): Boolean = Reflector.isPrimitive(erasure, extra)

  def isMap = classOf[Map[_, _]].isAssignableFrom(erasure)
  def isCollection = erasure.isArray || classOf[Iterable[_]].isAssignableFrom(erasure)
  def isOption = classOf[Option[_]].isAssignableFrom(erasure)
  def <:<(that: ScalaType): Boolean = manifest <:< that.manifest
  def >:>(that: ScalaType): Boolean = manifest >:> that.manifest

  override def hashCode(): Int = manifest.##

  override def equals(obj: Any): Boolean = obj match {
    case a: ScalaType => manifest == a.manifest
    case _ => false
  }

  def canEqual(that: Any): Boolean = that match {
    case s: ScalaType => manifest.canEqual(s.manifest)
    case _ => false
  }

  def copy(erasure: Class[_] = erasure, typeArgs: Seq[ScalaType] = typeArgs, typeVars: Map[TypeVariable[_], ScalaType] = typeVars) = {
    new ScalaType(ManifestFactory.manifestOf(erasure, typeArgs.map(_.manifest)))
  }

  override def toString: String = simpleName
}
case class PropertyDescriptor(name: String, mangledName: String, returnType: ScalaType, field: Field) extends Descriptor {
  def set(receiver: Any, value: Any) = field.set(receiver, value)
  def get(receiver: AnyRef) = field.get(receiver)
}
case class ConstructorParamDescriptor(name: String, mangledName: String, argIndex: Int, argType: ScalaType, defaultValue: Option[() => Any]) extends Descriptor {
  lazy val isOptional = defaultValue.isDefined || classOf[Option[_]].isAssignableFrom(argType.erasure)
}
case class ConstructorDescriptor(params: Seq[ConstructorParamDescriptor], constructor: java.lang.reflect.Constructor[_], isPrimary: Boolean) extends Descriptor
case class SingletonDescriptor(simpleName: String, fullName: String, erasure: ScalaType, instance: AnyRef, properties: Seq[PropertyDescriptor]) extends Descriptor

sealed trait ObjectDescriptor extends Descriptor
case class ClassDescriptor(simpleName: String, fullName: String, erasure: ScalaType, companion: Option[SingletonDescriptor], constructors: Seq[ConstructorDescriptor], properties: Seq[PropertyDescriptor]) extends ObjectDescriptor {

  def bestMatching(argNames: List[String]): Option[ConstructorDescriptor] = {
    val names = Set(argNames: _*)
    def countOptionals(args: List[ConstructorParamDescriptor]) =
      args.foldLeft(0)((n, x) => {
        val defv = companion flatMap {
          case so => Reflector.defaultValue(so.erasure.erasure, so.instance, argNames.indexOf(x.name))
        }
        if (x.isOptional || defv.isDefined) n+1 else n
      })
    def score(args: List[ConstructorParamDescriptor]) =
      args.foldLeft(0)((s, arg) => if (names.contains(arg.name)) s+1 else -100)

    if (constructors.isEmpty) None
    else {
      val best = constructors.tail.foldLeft((constructors.head, score(constructors.head.params.toList))) { (best, c) =>
        val newScore = score(c.params.toList)
        val newIsBetter =
          (newScore == best._2 && countOptionals(c.params.toList) < countOptionals(best._1.params.toList)) || newScore > best._2
        if (newIsBetter) (c, newScore) else best
      }
      Some(best._1)
    }
  }

  lazy val mostComprehensive: Seq[ConstructorParamDescriptor] = constructors.sortBy(-_.params.size).head.params
}

case class PrimitiveDescriptor(erasure: ScalaType, default: Option[() => Any] = None) extends ObjectDescriptor

