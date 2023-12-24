package org.json4s.reflect

import java.lang.reflect.{Field, TypeVariable}

import org.json4s.{JArray, JObject, JValue}

object ScalaType {

  private val types = new Memo[Manifest[?], ScalaType]

  private val singletonFieldName = "MODULE$"

  def apply[T](mf: Manifest[T]): ScalaType = {
    /* optimization */
    if (mf.runtimeClass == classOf[Int] || mf.runtimeClass == classOf[java.lang.Integer]) ScalaType.IntType
    else if (mf.runtimeClass == classOf[Long] || mf.runtimeClass == classOf[java.lang.Long]) ScalaType.LongType
    else if (mf.runtimeClass == classOf[Byte] || mf.runtimeClass == classOf[java.lang.Byte]) ScalaType.ByteType
    else if (mf.runtimeClass == classOf[Short] || mf.runtimeClass == classOf[java.lang.Short]) ScalaType.ShortType
    else if (mf.runtimeClass == classOf[Float] || mf.runtimeClass == classOf[java.lang.Float]) ScalaType.FloatType
    else if (mf.runtimeClass == classOf[Double] || mf.runtimeClass == classOf[java.lang.Double]) ScalaType.DoubleType
    else if (mf.runtimeClass == classOf[BigInt] || mf.runtimeClass == classOf[java.math.BigInteger])
      ScalaType.BigIntType
    else if (mf.runtimeClass == classOf[BigDecimal] || mf.runtimeClass == classOf[java.math.BigDecimal])
      ScalaType.BigDecimalType
    else if (mf.runtimeClass == classOf[Boolean] || mf.runtimeClass == classOf[java.lang.Boolean]) ScalaType.BooleanType
    else if (mf.runtimeClass == classOf[String] || mf.runtimeClass == classOf[java.lang.String]) ScalaType.StringType
    else if (mf.runtimeClass == classOf[java.util.Date]) ScalaType.DateType
    else if (mf.runtimeClass == classOf[java.sql.Timestamp]) ScalaType.TimestampType
    else if (mf.runtimeClass == classOf[Symbol]) ScalaType.SymbolType
    else if (mf.runtimeClass == classOf[Number]) ScalaType.NumberType
    else if (mf.runtimeClass == classOf[JObject]) ScalaType.JObjectType
    else if (mf.runtimeClass == classOf[JArray]) ScalaType.JArrayType
    else if (mf.runtimeClass == classOf[JValue]) ScalaType.JValueType
    /* end optimization */
    else {
      if (mf.typeArguments.isEmpty) types(mf, new ScalaType(_))
      else new ScalaType(mf)
    }
  }

  def apply(erasure: Class[?], typeArgs: Seq[ScalaType] = Seq.empty): ScalaType = {
    val mf = ManifestFactory.manifestOf(erasure, typeArgs.map(_.manifest))
    ScalaType(mf)
  }

  def apply(target: TypeInfo): ScalaType = {
    target match {
      case t: TypeInfo with SourceType => t.scalaType
      case t =>
        val tArgs =
          t.parameterizedType.map(_.getActualTypeArguments.toList.map(Reflector.scalaTypeOf(_))).getOrElse(Nil)
        ScalaType(target.clazz, tArgs)
    }
  }

  // Deal with the most common cases as an optimization
  /* optimization */
  private val IntType: ScalaType = new PrimitiveScalaType(Manifest.Int)
  private val NumberType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[Number]))
  private val LongType: ScalaType = new PrimitiveScalaType(Manifest.Long)
  private val ByteType: ScalaType = new PrimitiveScalaType(Manifest.Byte)
  private val ShortType: ScalaType = new PrimitiveScalaType(Manifest.Short)
  private val BooleanType: ScalaType = new PrimitiveScalaType(Manifest.Boolean)
  private val FloatType: ScalaType = new PrimitiveScalaType(Manifest.Float)
  private val DoubleType: ScalaType = new PrimitiveScalaType(Manifest.Double)
  private val StringType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[java.lang.String]))
  private val SymbolType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[Symbol]))
  private val BigDecimalType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[BigDecimal]))
  private val BigIntType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[BigInt]))
  private val JValueType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[JValue]))
  private val JObjectType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[JObject]))
  private val JArrayType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[JArray]))
  private val DateType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[java.util.Date]))
  private val TimestampType: ScalaType = new PrimitiveScalaType(Manifest.classType(classOf[java.sql.Timestamp]))

  private[json4s] val ListObject: ScalaType =
    new ScalaType(
      Manifest.classType(
        classOf[List[?]],
        Manifest.Object
      )
    )

  private[json4s] val Object: ScalaType =
    new ScalaType(Manifest.Object)

  private[json4s] val MapStringObject: ScalaType =
    new ScalaType(
      Manifest.classType(
        classOf[Map[?, ?]],
        Manifest.classType(classOf[String]),
        Manifest.Object
      )
    )

  private class PrimitiveScalaType(mf: Manifest[?]) extends ScalaType(mf) {
    override val isPrimitive = true
  }

  private class CopiedScalaType(
    mf: Manifest[?],
    private[this] var _typeVars: Map[String, ScalaType],
    override val isPrimitive: Boolean
  ) extends ScalaType(mf) {

    override def typeVars: Map[String, ScalaType] = {
      if (_typeVars == null)
        _typeVars = Map.empty ++
          erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[?]].getName).zip(typeArgs)
      _typeVars
    }
  }
  /* end optimization */
}

class ScalaType(val manifest: Manifest[?]) extends Equals {

  import ScalaType.{types, CopiedScalaType}
  val erasure: Class[?] = manifest.runtimeClass

  val typeArgs: Seq[ScalaType] = manifest.typeArguments.map(ta => Reflector.scalaTypeOf(ta)) ++ (
    if (erasure.isArray) List(Reflector.scalaTypeOf(erasure.getComponentType)) else Nil
  )

  private[this] var _typeVars: Map[String, ScalaType] = null
  def typeVars: Map[String, ScalaType] = {
    if (_typeVars == null)
      _typeVars = Map.empty ++
        erasure.getTypeParameters.map(_.asInstanceOf[TypeVariable[?]].getName).zip(typeArgs)
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
    if (_rawSimpleName == null) {
      _rawSimpleName = safeSimpleName(erasure)
    }
    _rawSimpleName
  }

  lazy val simpleName: String =
    rawSimpleName + (
      if (typeArgs.nonEmpty) typeArgs.map(_.simpleName).mkString("[", ", ", "]")
      else if (typeVars.nonEmpty) typeVars.map(_._2.simpleName).mkString("[", ", ", "]")
      else ""
    )

  lazy val fullName: String =
    rawFullName + (if (typeArgs.nonEmpty) typeArgs.map(_.fullName).mkString("[", ", ", "]") else "")

  lazy val typeInfo: TypeInfo =
    new TypeInfo(
      erasure,
      if (typeArgs.nonEmpty) Some(Reflector.mkParameterizedType(erasure, typeArgs.map(_.erasure))) else None
    ) with SourceType {
      val scalaType: ScalaType = ScalaType.this
    }

  val isPrimitive = false

  def isMap: Boolean = classOf[collection.immutable.Map[?, ?]].isAssignableFrom(erasure) ||
    classOf[collection.Map[?, ?]].isAssignableFrom(erasure)

  def isMutableMap: Boolean = classOf[collection.mutable.Map[?, ?]].isAssignableFrom(erasure)

  def isCollection: Boolean = erasure.isArray ||
    classOf[Iterable[?]].isAssignableFrom(erasure) ||
    classOf[java.util.Collection[?]].isAssignableFrom(erasure)

  def isOption: Boolean = classOf[Option[?]].isAssignableFrom(erasure)

  def isEither: Boolean = classOf[Either[?, ?]].isAssignableFrom(erasure)

  def <:<(that: ScalaType): Boolean = manifest <:< that.manifest

  def >:>(that: ScalaType): Boolean = manifest >:> that.manifest

  private def singletonField: Option[Field] = erasure.getFields.find(_.getName.equals(ScalaType.singletonFieldName))

  def isSingleton: Boolean = singletonField.isDefined

  def singletonInstance: Option[AnyRef] = singletonField.map(_.get(null))

  override def hashCode(): Int = manifest.##

  override def equals(obj: Any): Boolean = obj match {
    case a: ScalaType => manifest == a.manifest
    case _ => false
  }

  def canEqual(that: Any): Boolean = that match {
    case s: ScalaType => manifest.canEqual(s.manifest)
    case _ => false
  }

  def copy(
    erasure: Class[?] = erasure,
    typeArgs: Seq[ScalaType] = typeArgs,
    typeVars: Map[String, ScalaType] = _typeVars
  ): ScalaType = {

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
    else if (erasure == classOf[Number]) ScalaType.NumberType
    else if (erasure == classOf[JObject]) ScalaType.JObjectType
    else if (erasure == classOf[JArray]) ScalaType.JArrayType
    else if (erasure == classOf[JValue]) ScalaType.JValueType
    /* end optimization */
    else {
      val mf = ManifestFactory.manifestOf(erasure, typeArgs.map(_.manifest))
      val st = new CopiedScalaType(mf, typeVars, isPrimitive)
      if (typeArgs.isEmpty) types.replace(mf, st)
      else st
    }
  }

  override def toString: String = simpleName
}
