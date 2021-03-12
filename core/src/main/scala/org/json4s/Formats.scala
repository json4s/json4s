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

import java.util.{Date, TimeZone}

import reflect.ScalaType

import java.lang.reflect.Type

import org.json4s.prefs.EmptyValueStrategy
import org.json4s.prefs.ExtractionNullStrategy
import org.json4s.reflect.Reflector

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.reflect.classTag

object Formats {

  def read[T](json: JValue)(implicit reader: Reader[T]): T = reader.read(json)

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
      .collectFirst { case (x) if x.serialize.isDefinedAt(a) => x.serialize }
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
  def customSerializers: List[Serializer[_]] = Nil
  def richSerializers: List[RichSerializer[_]] = Nil
  def customKeySerializers: List[KeySerializer[_]] = Nil
  def fieldSerializers: List[(Class[_], FieldSerializer[_])] = Nil
  def wantsBigInt: Boolean = true
  def wantsBigDecimal: Boolean = false
  def primitives: Set[Type] = Set(classOf[JValue], classOf[JObject], classOf[JArray])
  def companions: List[(Class[_], AnyRef)] = Nil
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
   * Parameter name reading strategy. By default 'paranamer' is used.
   */
  def parameterNameReader: reflect.ParameterNameReader = reflect.ParanamerReader

  def emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default

  private def copy(
    wDateFormat: DateFormat = self.dateFormat,
    wParameterNameReader: reflect.ParameterNameReader = self.parameterNameReader,
    wTypeHints: TypeHints = self.typeHints,
    wCustomSerializers: List[Serializer[_]] = self.customSerializers,
    wCustomKeySerializers: List[KeySerializer[_]] = self.customKeySerializers,
    wFieldSerializers: List[(Class[_], FieldSerializer[_])] = self.fieldSerializers,
    wRichSerializers: List[RichSerializer[_]] = self.richSerializers,
    wWantsBigInt: Boolean = self.wantsBigInt,
    wWantsBigDecimal: Boolean = self.wantsBigDecimal,
    withPrimitives: Set[Type] = self.primitives,
    wCompanions: List[(Class[_], AnyRef)] = self.companions,
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
      override def customSerializers: List[Serializer[_]] = wCustomSerializers
      override def richSerializers: List[RichSerializer[_]] = wRichSerializers
      override val customKeySerializers: List[KeySerializer[_]] = wCustomKeySerializers
      override def fieldSerializers: List[(Class[_], FieldSerializer[_])] = wFieldSerializers
      override def wantsBigInt: Boolean = wWantsBigInt
      override def wantsBigDecimal: Boolean = wWantsBigDecimal
      override def primitives: Set[Type] = withPrimitives
      override def companions: List[(Class[_], AnyRef)] = wCompanions
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

  def withCompanions(comps: (Class[_], AnyRef)*): Formats = copy(wCompanions = comps.toList ::: self.companions)

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
  def +(newSerializer: RichSerializer[_]): Formats = copy(wRichSerializers = newSerializer :: self.richSerializers)

  /**
   * Adds the specified custom serializer to this formats.
   */
  def +(newSerializer: Serializer[_]): Formats = copy(wCustomSerializers = newSerializer :: self.customSerializers)

  /**
   * Adds the specified custom key serializer to this formats.
   */
  def +(newSerializer: KeySerializer[_]): Formats =
    copy(wCustomKeySerializers = newSerializer :: self.customKeySerializers)

  /**
   * Adds the specified custom serializers to this formats.
   */
  def ++(newSerializers: Iterable[Serializer[_]]): Formats =
    copy(wCustomSerializers = newSerializers.foldRight(self.customSerializers)(_ :: _))

  /**
   * Removes the specified custom serializer from this formats.
   */
  def -(serializer: Serializer[_]): Formats =
    copy(wCustomSerializers = self.customSerializers.filterNot(_ == serializer))

  /**
   * Adds the specified custom serializers to this formats.
   */
  def addKeySerializers(newKeySerializers: Iterable[KeySerializer[_]]): Formats =
    newKeySerializers.foldLeft(this)(_ + _)

  /**
   * Adds a field serializer for a given type to this formats.
   */
  def +[A](newSerializer: FieldSerializer[A]): Formats =
    copy(wFieldSerializers = (newSerializer.mf.runtimeClass -> newSerializer) :: self.fieldSerializers)

  private[json4s] def fieldSerializer(clazz: Class[_]): Option[FieldSerializer[_]] = {
    import ClassDelta._

    val ord = Ordering[Int].on[(Class[_], FieldSerializer[_])](x => delta(x._1, clazz))
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

trait RichSerializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), A]
  def serialize(implicit format: Formats): PartialFunction[Any, JValue]
}

trait Serializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), A]
  def serialize(implicit format: Formats): PartialFunction[Any, JValue]
}

trait KeySerializer[A] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, String), A]
  def serialize(implicit format: Formats): PartialFunction[Any, String]
}

/**
 * Type hints can be used to alter the default conversion rules when converting
 * Scala instances into JSON and vice versa. Type hints must be used when converting
 * class which is not supported by default (for instance when class is not a case class).
 * <p>
 * Example:<pre>
 * class DateTime(val time: Long)
 *
 * val hints = new ShortTypeHints(classOf[DateTime] :: Nil) {
 *   override def serialize: PartialFunction[Any, JObject] = {
 *     case t: DateTime => JObject(JField("t", JInt(t.time)) :: Nil)
 *   }
 *
 *   override def deserialize: PartialFunction[(String, JObject), Any] = {
 *     case ("DateTime", JObject(JField("t", JInt(t)) :: Nil)) => new DateTime(t.longValue)
 *   }
 * }
 * implicit val formats: Formats = DefaultFormats.withHints(hints)
 * </pre>
 */
trait TypeHints {

  val hints: List[Class[_]]

  /**
   * Return hint for given type.
   */
  def hintFor(clazz: Class[_]): Option[String]

  /**
   * Return type for given hint.
   */
  def classFor(hint: String, parent: Class[_]): Option[Class[_]]

  /**
   * The name of the field in JSON where type hints are added (jsonClass by default)
   */
  def typeHintFieldName: String = "jsonClass"

  def isTypeHintField(f: JField, parent: Class[_]): Boolean = f match {
    case (key, JString(value)) =>
      val hint = typeHintFieldNameForHint(value, parent)
      key == typeHintFieldName && hint.isDefined
    case _ => false
  }
  def typeHintFieldNameForHint(hint: String, parent: Class[_]): Option[String] =
    classFor(hint, parent) map (_ => typeHintFieldName)
  def typeHintFieldNameForClass(clazz: Class[_]): Option[String] =
    hintFor(clazz).flatMap(typeHintFieldNameForHint(_, clazz))
  def containsHint(clazz: Class[_]): Boolean =
    hints exists (_ isAssignableFrom clazz)
  def shouldExtractHints(clazz: Class[_]): Boolean =
    hints exists (clazz isAssignableFrom _)
  def deserialize: PartialFunction[(String, JObject), Any] = Map()
  def serialize: PartialFunction[Any, JObject] = Map()

  def components: List[TypeHints] = List(this)

  /**
   * Adds the specified type hints to this type hints.
   */
  def +(hints: TypeHints): TypeHints = TypeHints.CompositeTypeHints(hints.components ::: components)

}

private[json4s] object TypeHints {

  private case class CompositeTypeHints(override val components: List[TypeHints]) extends TypeHints {
    val hints: List[Class[_]] = components.flatMap(_.hints)

    /**
     * Chooses most specific class.
     */
    def hintFor(clazz: Class[_]): Option[String] = {
      (components.reverse
      filter (_.containsHint(clazz))
      map { th =>
        val hint = th.hintFor(clazz)
        (
          hint,
          hint
            .flatMap(th.classFor(_, clazz))
            .getOrElse(
              sys.error("hintFor/classFor not invertible for " + th)
            )
        )
      }
      sortWith ((x, y) => (ClassDelta.delta(x._2, clazz) - ClassDelta.delta(y._2, clazz)) <= 0)).headOption
        .flatMap(_._1)
    }

    def classFor(hint: String, parent: Class[_]): Option[Class[_]] = {
      def hasClass(h: TypeHints) =
        scala.util.control.Exception.allCatch opt (h.classFor(hint, parent)) exists (_.isDefined)

      components find hasClass flatMap (_.classFor(hint, parent))
    }

    override def isTypeHintField(f: JField, parent: Class[_]): Boolean =
      components exists (_.isTypeHintField(f, parent))

    override def typeHintFieldNameForHint(hint: String, parent: Class[_]): Option[String] =
      components.flatMap(_.typeHintFieldNameForHint(hint, parent)).headOption

    override def typeHintFieldNameForClass(clazz: Class[_]): Option[String] =
      components.flatMap(_.typeHintFieldNameForClass(clazz)).headOption

    override def deserialize: PartialFunction[(String, JObject), Any] =
      components.foldLeft[PartialFunction[(String, JObject), Any]](Map()) { (result, cur) =>
        result.orElse(cur.deserialize)
      }

    override def serialize: PartialFunction[Any, JObject] =
      components.foldLeft[PartialFunction[Any, JObject]](Map()) { (result, cur) =>
        result.orElse(cur.serialize)
      }
  }

}

private[json4s] object ClassDelta {
  def delta(class1: Class[_], class2: Class[_]): Int = {
    if (class1 == class2) 0
    else if (class1 == null) 1
    else if (class2 == null) -1
    else if (class1.getInterfaces.contains(class2)) 0
    else if (class2.getInterfaces.contains(class1)) 0
    else if (class1.isAssignableFrom(class2)) {
      1 + delta(class1, class2.getSuperclass)
    } else if (class2.isAssignableFrom(class1)) {
      1 + delta(class1.getSuperclass, class2)
    } else sys.error("Don't call delta unless one class is assignable from the other")
  }
}

/**
 * Do not use any type hints.
 */
case object NoTypeHints extends TypeHints {
  val hints: List[Class[_]] = Nil
  def hintFor(clazz: Class[_]) = None
  def classFor(hint: String, parent: Class[_]) = None
  override def shouldExtractHints(clazz: Class[_]) = false
}

/**
 * Use short class name as a type hint.
 */
case class ShortTypeHints(hints: List[Class[_]], override val typeHintFieldName: String = "jsonClass")
  extends TypeHints {
  def hintFor(clazz: Class[_]) =
    Some(clazz.getName.substring(clazz.getName.lastIndexOf(".") + 1))
  def classFor(hint: String, parent: Class[_]) = hints find (hintFor(_).exists(_ == hint))
}

/**
 * Use full class name as a type hint.
 */
case class FullTypeHints(hints: List[Class[_]], override val typeHintFieldName: String = "jsonClass")
  extends TypeHints {
  def hintFor(clazz: Class[_]) = Some(clazz.getName)
  def classFor(hint: String, parent: Class[_]) = {
    Reflector
      .scalaTypeOf(hint)
      .find(h => hints.exists(l => l.isAssignableFrom(h.erasure)))
      .map(_.erasure)
  }
}

/**
 * Use a map of keys as type hints.  Values may not be mapped by multiple keys
 */
case class MappedTypeHints(hintMap: Map[Class[_], String], override val typeHintFieldName: String = "jsonClass")
  extends TypeHints {
  require(hintMap.size == hintMap.values.toList.distinct.size, "values in type hint mapping must be distinct")

  override val hints: List[Class[_]] = hintMap.keys.toList
  private val lookup: Map[String, Class[_]] = hintMap.map(_.swap)

  def hintFor(clazz: Class[_]) = hintMap.get(clazz)
  def classFor(hint: String, parent: Class[_]) = lookup.get(hint).filter(parent.isAssignableFrom)
}

/**
 * Default date format is UTC time.
 */
object DefaultFormats extends DefaultFormats {
  val UTC = TimeZone.getTimeZone("UTC")

  val losslessDate = {
    def createSdf = {
      val f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      f.setTimeZone(UTC)
      f
    }
    new ThreadLocal(createSdf)
  }

}

private[json4s] class ThreadLocal[A](init: => A) extends java.lang.ThreadLocal[A] with (() => A) {
  override def initialValue = init
  def apply() = get
}
trait DefaultFormats extends Formats {
  import java.text.{ParseException, SimpleDateFormat}

  private[this] val df = new ThreadLocal[SimpleDateFormat](dateFormatter)

  override val parameterNameReader: reflect.ParameterNameReader = reflect.ParanamerReader
  override val typeHints: TypeHints = NoTypeHints
  override val customSerializers: List[Serializer[_]] = Nil
  override val customKeySerializers: List[KeySerializer[_]] = Nil
  override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = Nil
  override val wantsBigInt: Boolean = true
  override val wantsBigDecimal: Boolean = false
  override val primitives: Set[Type] = Set(classOf[JValue], classOf[JObject], classOf[JArray])
  override val companions: List[(Class[_], AnyRef)] = Nil
  override val strictOptionParsing: Boolean = false
  override val emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default
  override val extractionNullStrategy: ExtractionNullStrategy = ExtractionNullStrategy.Keep
  override def strictFieldDeserialization: Boolean = false

  val dateFormat: DateFormat = new DateFormat {
    def parse(s: String) = try {
      Some(formatter.parse(s))
    } catch {
      case _: ParseException => None
    }

    def format(d: Date) = formatter.format(d)

    def timezone = formatter.getTimeZone

    private[this] def formatter = df()
  }

  protected def dateFormatter: SimpleDateFormat = {
    val f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    f.setTimeZone(DefaultFormats.UTC)
    f
  }

  /**
   * Lossless date format includes milliseconds too.
   */
  def lossless: Formats = new DefaultFormats {
    override def dateFormatter = DefaultFormats.losslessDate()
  }

  /**
   * Default formats with given <code>TypeHint</code>s.
   */
  def withHints(hints: TypeHints): Formats = new DefaultFormats {
    override val typeHints = hints
  }
}

class CustomSerializer[A: ClassTag](ser: Formats => (PartialFunction[JValue, A], PartialFunction[Any, JValue]))
  extends Serializer[A] {

  val Class = classTag[A].runtimeClass

  def deserialize(implicit format: Formats) = { case (TypeInfo(Class, _), json) =>
    if (ser(format)._1.isDefinedAt(json)) ser(format)._1(json)
    else throw new MappingException("Can't convert " + json + " to " + Class)
  }

  def serialize(implicit format: Formats) = ser(format)._2
}

class CustomKeySerializer[A: ClassTag](ser: Formats => (PartialFunction[String, A], PartialFunction[Any, String]))
  extends KeySerializer[A] {

  val Class = classTag[A].runtimeClass

  def deserialize(implicit format: Formats) = { case (TypeInfo(Class, _), json) =>
    if (ser(format)._1.isDefinedAt(json)) ser(format)._1(json)
    else throw new MappingException("Can't convert " + json + " to " + Class)
  }

  def serialize(implicit format: Formats) = ser(format)._2
}
