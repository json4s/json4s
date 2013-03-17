package org.json4s
package reflect

import java.lang.reflect.{Constructor => JConstructor, Type, Field, TypeVariable}
import scala._

sealed trait Descriptor
object ScalaType {

  private val types = new Memo[Manifest[_], ScalaType]

  def apply[T](mf: Manifest[T]): ScalaType = {
    /* optimization */
    if (mf.erasure == classOf[Int] || mf.erasure == classOf[java.lang.Integer]) ScalaType.IntType
    else if (mf.erasure == classOf[Long] || mf.erasure == classOf[java.lang.Long]) ScalaType.LongType
    else if (mf.erasure == classOf[Byte] || mf.erasure == classOf[java.lang.Byte]) ScalaType.ByteType
    else if (mf.erasure == classOf[Short] || mf.erasure == classOf[java.lang.Short]) ScalaType.ShortType
    else if (mf.erasure == classOf[Float] || mf.erasure == classOf[java.lang.Float]) ScalaType.FloatType
    else if (mf.erasure == classOf[Double] || mf.erasure == classOf[java.lang.Double]) ScalaType.DoubleType
    else if (mf.erasure == classOf[BigInt] || mf.erasure == classOf[java.math.BigInteger]) ScalaType.BigIntType
    else if (mf.erasure == classOf[BigDecimal] || mf.erasure == classOf[java.math.BigDecimal]) ScalaType.BigDecimalType
    else if (mf.erasure == classOf[Boolean] || mf.erasure == classOf[java.lang.Boolean]) ScalaType.BooleanType
    else if (mf.erasure == classOf[String] || mf.erasure == classOf[java.lang.String]) ScalaType.StringType
    else if (mf.erasure == classOf[java.util.Date]) ScalaType.DateType
    else if (mf.erasure == classOf[java.sql.Timestamp]) ScalaType.TimestampType
    else if (mf.erasure == classOf[Symbol]) ScalaType.SymbolType
    else if (mf.erasure == classOf[JObject]) ScalaType.JObjectType
    else if (mf.erasure == classOf[JArray]) ScalaType.JArrayType
    else if (mf.erasure == classOf[JValue]) ScalaType.JValueType
    /* end optimization */
    else {
      if (!mf.erasure.isArray) types(mf, new ScalaType(_))
      else {
        val nmf = ManifestFactory.manifestOf(mf.erasure, List(ManifestFactory.manifestOf(mf.erasure.getComponentType)))
        types(nmf, new ScalaType(_))
      }
    }
  }

  def apply(erasure: Class[_], typeArgs: Seq[ScalaType] = Seq.empty): ScalaType = {
    val mf = ManifestFactory.manifestOf(erasure, typeArgs.map(_.manifest))
    ScalaType(mf)
  }

  def apply(target: TypeInfo): ScalaType = {
    target match {
      case t: TypeInfo with SourceType => t.scalaType
      case t =>
        val tArgs = t.parameterizedType.map(_.getActualTypeArguments.toList.map(Reflector.scalaTypeOf(_))).getOrElse(Nil)
        ScalaType(target.clazz, tArgs)
    }
  }

  // Deal with the most common cases as an optimization
  /* optimization */
  private val IntType: ScalaType = new ScalaType(Manifest.Int) { override val isPrimitive = true }
  private val LongType: ScalaType = new ScalaType(Manifest.Long) { override val isPrimitive = true }
  private val ByteType: ScalaType = new ScalaType(Manifest.Byte) { override val isPrimitive = true }
  private val ShortType: ScalaType = new ScalaType(Manifest.Short) { override val isPrimitive = true }
  private val BooleanType: ScalaType = new ScalaType(Manifest.Boolean) { override val isPrimitive = true }
  private val FloatType: ScalaType = new ScalaType(Manifest.Float) { override val isPrimitive = true }
  private val DoubleType: ScalaType = new ScalaType(Manifest.Double) { override val isPrimitive = true }
  private val StringType: ScalaType = new ScalaType(manifest[String]) { override val isPrimitive = true }
  private val SymbolType: ScalaType = new ScalaType(manifest[Symbol]) { override val isPrimitive = true }
  private val BigDecimalType: ScalaType = new ScalaType(manifest[BigDecimal]) { override val isPrimitive = true }
  private val BigIntType: ScalaType = new ScalaType(manifest[BigInt]) { override val isPrimitive = true }
  private val JValueType: ScalaType = new ScalaType(manifest[JValue]) { override val isPrimitive = true }
  private val JObjectType: ScalaType = new ScalaType(manifest[JObject]) { override val isPrimitive = true }
  private val JArrayType: ScalaType = new ScalaType(manifest[JArray]) { override val isPrimitive = true }
  private val DateType: ScalaType = new ScalaType(manifest[java.util.Date]) { override val isPrimitive = true }
  private val TimestampType: ScalaType = new ScalaType(manifest[java.sql.Timestamp]) { override val isPrimitive = true }
  /* end optimization */
}
class ScalaType(private val manifest: Manifest[_]) extends Equals {

  import ScalaType.types
  private[this] val self = this

  private[this] var _erasure: Class[_] = null  // Don't use a lazy value, optimization
  def erasure: Class[_] = {
    if (_erasure == null)
      _erasure = manifest.erasure
    _erasure
  }

  private[this] var _typeArgs: Seq[ScalaType] = null
  def typeArgs: Seq[ScalaType] = {
    if (_typeArgs == null)
      _typeArgs = manifest.typeArguments.map(ta => Reflector.scalaTypeOf(ta)) ++ (
        if (erasure.isArray) List(Reflector.scalaTypeOf(erasure.getComponentType)) else Nil
      )
    _typeArgs
  }

  private[this] var _typeVars: Map[TypeVariable[_], ScalaType] = null
  def typeVars: Map[TypeVariable[_], ScalaType] = {
    if (_typeVars == null)
      _typeVars = Map.empty ++
        erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[_]]).toList.zip(manifest.typeArguments map (ScalaType(_)))
    _typeVars
  }

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

  val isPrimitive = false

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

  def copy(erasure: Class[_] = _erasure, typeArgs: Seq[ScalaType] = _typeArgs, typeVars: Map[TypeVariable[_], ScalaType] = _typeVars): ScalaType = {
    /* optimization */
    if (erasure == classOf[Int] || erasure == classOf[java.lang.Integer]) ScalaType.IntType
    else if (erasure == classOf[Long] || erasure == classOf[java.lang.Long]) ScalaType.LongType
    else if (erasure == classOf[Byte] || erasure == classOf[java.lang.Byte]) ScalaType.ByteType
    else if (erasure == classOf[Short] || erasure == classOf[java.lang.Short]) ScalaType.ShortType
    else if (erasure == classOf[Float] || erasure == classOf[java.lang.Float]) ScalaType.FloatType
    else if (erasure == classOf[Double] || erasure == classOf[java.lang.Double]) ScalaType.DoubleType
    else if (erasure == classOf[BigInt] || erasure == classOf[java.math.BigInteger]) ScalaType.BigIntType
    else if (erasure == classOf[BigDecimal] || erasure == classOf[java.math.BigDecimal]) ScalaType.BigDecimalType
    else if (erasure == classOf[Boolean] || erasure == classOf[java.lang.Boolean]) ScalaType.BooleanType
    else if (erasure == classOf[String] || erasure == classOf[java.lang.String]) ScalaType.StringType
    else if (erasure == classOf[java.util.Date]) ScalaType.DateType
    else if (erasure == classOf[java.sql.Timestamp]) ScalaType.TimestampType
    else if (erasure == classOf[Symbol]) ScalaType.SymbolType
    else if (erasure == classOf[JObject]) ScalaType.JObjectType
    else if (erasure == classOf[JArray]) ScalaType.JArrayType
    else if (erasure == classOf[JValue]) ScalaType.JValueType
    /* end optimization */
    else {
      val e = erasure
      val ta = typeArgs
      val tv = typeVars
      val prim = isPrimitive
      val st = new ScalaType(ManifestFactory.manifestOf(erasure, typeArgs.map(_.manifest))) {
        private[this] var _erasure: Class[_] = e
        override def erasure: Class[_] = {
          if (_erasure == null)
            _erasure = manifest.erasure
          _erasure
        }

        private[this] var _typeArgs: Seq[ScalaType] = ta
        override def typeArgs: Seq[ScalaType] = {
          if (_typeArgs == null)
            _typeArgs = manifest.typeArguments.map(ta => Reflector.scalaTypeOf(ta)) ++ (
              if (erasure.isArray) List(Reflector.scalaTypeOf(erasure.getComponentType)) else Nil
            )
          _typeArgs
        }

        private[this] var _typeVars: Map[TypeVariable[_], ScalaType] = tv
        override def typeVars: Map[TypeVariable[_], ScalaType] = {
          if (_typeVars == null)
            _typeVars = Map.empty ++
              erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[_]]).toList.zip(manifest.typeArguments map (ScalaType(_)))
          _typeVars
        }
        override val isPrimitive = prim
      }
      types.replace(manifest, st)
    }
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

