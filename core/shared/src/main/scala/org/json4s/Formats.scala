/*
 * Copyright 2009-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s

import reflect.ScalaType

import java.lang.reflect.Type

import org.json4s.prefs.EmptyValueStrategy
import org.json4s.prefs.ExtractionNullStrategy

import scala.annotation.implicitNotFound

object Formats {

  def read[T](json: JValue)(implicit reader: Reader[T]): T = reader.readEither(json) match {
    case Right(x) => x
    case Left(x) => throw x
  }

  def write[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)

  // ---------------------------------
  // internal utilities

  private[json4s] def customSerializer(a: Any)(implicit format: Formats): PartialFunction[Any, JValue] = {
    format.customSerializers
      .collectFirst { case x if x.serialize.isDefinedAt(a) => x.serialize }
      .getOrElse(PartialFunction.empty[Any, JValue])
  }

  private[json4s] def customRichDeserializer(
    a: (ScalaType, JValue)
  )(implicit format: Formats): PartialFunction[(ScalaType, JValue), Any] = {
    format.richSerializers
      .collectFirst { case x if x.deserialize.isDefinedAt(a) => x.deserialize }
      .getOrElse(PartialFunction.empty[(ScalaType, JValue), Any])
  }

  private[json4s] def customRichSerializer(a: Any)(implicit format: Formats): PartialFunction[Any, JValue] = {
    format.richSerializers
      .collectFirst { case x if x.serialize.isDefinedAt(a) => x.serialize }
      .getOrElse(PartialFunction.empty[Any, JValue])
  }

  private[json4s] def customDeserializer(
    a: (TypeInfo, JValue)
  )(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Any] = {
    format.customSerializers
      .collectFirst { case x if x.deserialize.isDefinedAt(a) => x.deserialize }
      .getOrElse(PartialFunction.empty[(TypeInfo, JValue), Any])
  }

  private[json4s] def customKeySerializer(a: Any)(implicit format: Formats): PartialFunction[Any, String] =
    format.customKeySerializers
      .collectFirst { case x if x.serialize.isDefinedAt(a) => x.serialize }
      .getOrElse(PartialFunction.empty[Any, String])

  private[json4s] def customKeyDeserializer(
    a: (TypeInfo, String)
  )(implicit format: Formats): PartialFunction[(TypeInfo, String), Any] =
    format.customKeySerializers
      .collectFirst { case x if x.deserialize.isDefinedAt(a) => x.deserialize }
      .getOrElse(PartialFunction.empty[(TypeInfo, String), Any])
  // ---------------------------------
}

/**
 * Formats to use when converting JSON.
 * Formats are usually configured by using an implicit parameter:
 * <pre>
 * implicit val formats: Formats = org.json4s.DefaultFormats
 * </pre>
 */
@implicitNotFound(
  "No org.json4s.Formats found. Try to bring an instance of org.json4s.Formats in scope or use the org.json4s.DefaultFormats."
)
trait Formats extends Serializable { self: Formats =>
  def dateFormat: DateFormat
  def typeHints: TypeHints = NoTypeHints
  def customSerializers: List[Serializer[?]] = Nil
  def richSerializers: List[RichSerializer[?]] = Nil
  def customKeySerializers: List[KeySerializer[?]] = Nil
  def fieldSerializers: List[(Class[?], FieldSerializer[?])] = Nil
  def wantsBigInt: Boolean = true
  def wantsBigDecimal: Boolean = false
  def primitives: Set[Type] = Set(classOf[JValue], classOf[JObject], classOf[JArray])
  def companions: List[(Class[?], AnyRef)] = Nil
  def extractionNullStrategy: ExtractionNullStrategy = ExtractionNullStrategy.Keep
  def strictOptionParsing: Boolean = false
  def strictArrayExtraction: Boolean = false
  def strictMapExtraction: Boolean = false
  def alwaysEscapeUnicode: Boolean = false
  def strictFieldDeserialization: Boolean = false

  /**
   * Setting to false preserves library's behavior prior to 3.6, where companion object constructors were only
   * considered when deserializing if there were no primary constructors. Setting to true preserves the
   * backwards-incompatible change made in 3.6 to always consider companion object constructors when deserializing
   * (https://github.com/json4s/json4s/pull/487).
   */
  def considerCompanionConstructors: Boolean = true

  /**
   * Parameter name reading strategy.
   */
  def parameterNameReader: reflect.ParameterNameReader = reflect.ParanamerReader

  def emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default

  private def copy(
    wDateFormat: DateFormat = self.dateFormat,
    wParameterNameReader: reflect.ParameterNameReader = self.parameterNameReader,
    wTypeHints: TypeHints = self.typeHints,
    wCustomSerializers: List[Serializer[?]] = self.customSerializers,
    wCustomKeySerializers: List[KeySerializer[?]] = self.customKeySerializers,
    wFieldSerializers: List[(Class[?], FieldSerializer[?])] = self.fieldSerializers,
    wRichSerializers: List[RichSerializer[?]] = self.richSerializers,
    wWantsBigInt: Boolean = self.wantsBigInt,
    wWantsBigDecimal: Boolean = self.wantsBigDecimal,
    withPrimitives: Set[Type] = self.primitives,
    wCompanions: List[(Class[?], AnyRef)] = self.companions,
    wExtractionNullStrategy: ExtractionNullStrategy = self.extractionNullStrategy,
    wStrictOptionParsing: Boolean = self.strictOptionParsing,
    wStrictArrayExtraction: Boolean = self.strictArrayExtraction,
    wStrictMapExtraction: Boolean = self.strictMapExtraction,
    wAlwaysEscapeUnicode: Boolean = self.alwaysEscapeUnicode,
    wConsiderCompanionConstructors: Boolean = self.considerCompanionConstructors,
    wEmptyValueStrategy: EmptyValueStrategy = self.emptyValueStrategy,
    wStrictFieldDeserialization: Boolean = self.strictFieldDeserialization
  ): Formats =
    new Formats {
      def dateFormat: DateFormat = wDateFormat
      override def parameterNameReader: reflect.ParameterNameReader = wParameterNameReader
      override def typeHints: TypeHints = wTypeHints
      override def customSerializers: List[Serializer[?]] = wCustomSerializers
      override def richSerializers: List[RichSerializer[?]] = wRichSerializers
      override val customKeySerializers: List[KeySerializer[?]] = wCustomKeySerializers
      override def fieldSerializers: List[(Class[?], FieldSerializer[?])] = wFieldSerializers
      override def wantsBigInt: Boolean = wWantsBigInt
      override def wantsBigDecimal: Boolean = wWantsBigDecimal
      override def primitives: Set[Type] = withPrimitives
      override def companions: List[(Class[?], AnyRef)] = wCompanions
      override def extractionNullStrategy: ExtractionNullStrategy = wExtractionNullStrategy
      override def strictOptionParsing: Boolean = wStrictOptionParsing
      override def strictArrayExtraction: Boolean = wStrictArrayExtraction
      override def strictMapExtraction: Boolean = wStrictMapExtraction
      override def alwaysEscapeUnicode: Boolean = wAlwaysEscapeUnicode
      override def considerCompanionConstructors: Boolean = wConsiderCompanionConstructors
      override def emptyValueStrategy: EmptyValueStrategy = wEmptyValueStrategy
      override def strictFieldDeserialization: Boolean = wStrictFieldDeserialization
    }

  def withBigInt: Formats = copy(wWantsBigInt = true)

  def withLong: Formats = copy(wWantsBigInt = false)

  def withBigDecimal: Formats = copy(wWantsBigDecimal = true)

  def withDouble: Formats = copy(wWantsBigDecimal = false)

  def withCompanions(comps: (Class[?], AnyRef)*): Formats = copy(wCompanions = comps.toList ::: self.companions)

  def preservingEmptyValues = withEmptyValueStrategy(EmptyValueStrategy.preserve)

  def skippingEmptyValues = withEmptyValueStrategy(EmptyValueStrategy.skip)

  def withEmptyValueStrategy(strategy: EmptyValueStrategy): Formats = copy(wEmptyValueStrategy = strategy)

  def withEscapeUnicode: Formats = copy(wAlwaysEscapeUnicode = true)

  def withStrictOptionParsing: Formats = copy(wStrictOptionParsing = true)

  def withStrictArrayExtraction: Formats = copy(wStrictArrayExtraction = true)

  def withStrictMapExtraction: Formats = copy(wStrictMapExtraction = true)

  /**
   * Prior to 3.6 companion object constructors were only considered when deserializing if there were no primary
   * constructors. A backwards-incompatible change was made in 3.6 to always consider companion object constructors
   * when deserializing (https://github.com/json4s/json4s/pull/487), and is the default setting
   * (considerCompanionConstructors = true). This changes the setting to false to preserve pre-3.6
   * deserialization behavior.
   */
  def withPre36DeserializationBehavior: Formats = copy(wConsiderCompanionConstructors = false)

  def strict: Formats = copy(wStrictOptionParsing = true, wStrictArrayExtraction = true, wStrictMapExtraction = true)

  def nonStrict: Formats =
    copy(wStrictOptionParsing = false, wStrictArrayExtraction = false, wStrictMapExtraction = false)

  @deprecated(message = "Use withNullExtractionStrategy instead", since = "3.7.0")
  def disallowNull: Formats = copy(wExtractionNullStrategy = ExtractionNullStrategy.Disallow)

  def withExtractionNullStrategy(strategy: ExtractionNullStrategy): Formats = copy(wExtractionNullStrategy = strategy)

  def withStrictFieldDeserialization: Formats = copy(wStrictFieldDeserialization = true)

  /**
   * Adds the specified type hints to this formats.
   */
  def +(extraHints: TypeHints): Formats = copy(wTypeHints = self.typeHints + extraHints)

  /**
   * Adds the specified custom serializer to this formats.
   */
  def +(newSerializer: RichSerializer[?]): Formats = copy(wRichSerializers = newSerializer :: self.richSerializers)

  /**
   * Adds the specified custom serializer to this formats.
   */
  def +(newSerializer: Serializer[?]): Formats = copy(wCustomSerializers = newSerializer :: self.customSerializers)

  /**
   * Adds the specified custom key serializer to this formats.
   */
  def +(newSerializer: KeySerializer[?]): Formats =
    copy(wCustomKeySerializers = newSerializer :: self.customKeySerializers)

  /**
   * Adds the specified custom serializers to this formats.
   */
  def ++(newSerializers: Iterable[Serializer[?]]): Formats =
    copy(wCustomSerializers = newSerializers.foldRight(self.customSerializers)(_ :: _))

  /**
   * Removes the specified custom serializer from this formats.
   */
  def -(serializer: Serializer[?]): Formats =
    copy(wCustomSerializers = self.customSerializers.filterNot(_ == serializer))

  /**
   * Adds the specified custom serializers to this formats.
   */
  def addKeySerializers(newKeySerializers: Iterable[KeySerializer[?]]): Formats =
    newKeySerializers.foldLeft(this)(_ + _)

  /**
   * Adds a field serializer for a given type to this formats.
   */
  def +[A](newSerializer: FieldSerializer[A]): Formats =
    copy(wFieldSerializers = (newSerializer.mf.runtimeClass -> newSerializer) :: self.fieldSerializers)

  private[json4s] def fieldSerializer(clazz: Class[?]): Option[FieldSerializer[?]] = {
    import ClassDelta._

    val ord = Ordering[Int].on[(Class[?], FieldSerializer[?])](x => delta(x._1, clazz))
    fieldSerializers filter (_._1.isAssignableFrom(clazz)) match {
      case Nil => None
      case xs => Some((xs min ord)._2)
    }
  }

  @deprecated(message = "Use the internal methods in the companion object instead.", since = "3.6.4")
  def customSerializer(implicit format: Formats): PartialFunction[Any, JValue] =
    customSerializers.foldLeft(Map(): PartialFunction[Any, JValue]) { (acc, x) =>
      acc.orElse(x.serialize)
    }

  @deprecated(message = "Use the internal methods in the companion object instead.", since = "3.6.4")
  def customDeserializer(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Any] =
    customSerializers.foldLeft(Map(): PartialFunction[(TypeInfo, JValue), Any]) { (acc, x) =>
      acc.orElse(x.deserialize)
    }

  @deprecated(message = "Use the internal methods in the companion object instead.", since = "3.6.4")
  def customKeySerializer(implicit format: Formats): PartialFunction[Any, String] =
    customKeySerializers.foldLeft(Map(): PartialFunction[Any, String]) { (acc, x) =>
      acc.orElse(x.serialize)
    }

  @deprecated(message = "Use the internal methods in the companion object instead.", since = "3.6.4")
  def customKeyDeserializer(implicit format: Formats): PartialFunction[(TypeInfo, String), Any] =
    customKeySerializers.foldLeft(Map(): PartialFunction[(TypeInfo, String), Any]) { (acc, x) =>
      acc.orElse(x.deserialize)
    }
}
