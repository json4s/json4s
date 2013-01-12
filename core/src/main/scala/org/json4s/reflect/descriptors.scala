package org.json4s.reflect

import java.lang.reflect.{Constructor => JConstructor, Type, Field, TypeVariable}
import org.json4s.{Meta, TypeInfo}

sealed trait Descriptor
object ScalaType {
  def apply[T](mf: Manifest[T]): ScalaType = {
    new ScalaType(
      mf.erasure,
      mf.typeArguments.map(ta => Reflector.scalaTypeOf(ta)),
      Map.empty ++
        mf.erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[_]]).toList.zip(mf.typeArguments map (ScalaType(_))))
  }
}
case class ScalaType(erasure: Class[_], typeArgs: Seq[ScalaType], typeVars: Map[TypeVariable[_], ScalaType]) extends Descriptor {
  lazy val rawFullName: String = erasure.getName
  lazy val rawSimpleName: String = erasure.getSimpleName
  lazy val simpleName: String = rawSimpleName + (if (typeArgs.nonEmpty) typeArgs.map(_.simpleName).mkString("[", ", ", "]") else "")
  lazy val fullName: String = rawFullName + (if (typeArgs.nonEmpty) typeArgs.map(_.fullName).mkString("[", ", ", "]") else "")
  lazy val typeInfo: TypeInfo = TypeInfo(erasure, if (typeArgs.nonEmpty) Some(Meta.mkParameterizedType(erasure, typeArgs.map(_.erasure).toSeq)) else None)
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
case class ClassDescriptor(simpleName: String, fullName: String, erasure: ScalaType, companion: Option[SingletonDescriptor], constructors: Seq[ConstructorDescriptor], properties: Seq[PropertyDescriptor]) extends Descriptor {
//    def bestConstructor(argNames: Seq[String]): Option[ConstructorDescriptor] = {
//      constructors.sortBy(-_.params.size)
//    }
  lazy val mostComprehensive: Seq[ConstructorParamDescriptor] = constructors.sortBy(-_.params.size).head.params
}
