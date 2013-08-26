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
import reflect.Reflector
import java.util
import scala.collection.JavaConverters._
import scala.util.DynamicVariable
import annotation.implicitNotFound
import java.lang.reflect.Type

object Formats {
  def read[T](json: JValue)(implicit reader: Reader[T]): T = reader.read(json)
  def write[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)
}

/** Formats to use when converting JSON.
 * Formats are usually configured by using an implicit parameter:
 * <pre>
 * implicit val formats = org.json4s.DefaultFormats
 * </pre>
 */
@implicitNotFound(
  "No org.json4s.Formats found. Try to bring an instance of org.json4s.Formats in scope or use the org.json4s.DefaultFormats."
)
trait Formats { self: Formats =>
  def dateFormat: DateFormat
  def typeHints: TypeHints = NoTypeHints
  def customSerializers: List[Serializer[_]] = Nil
  def fieldSerializers: List[(Class[_], FieldSerializer[_])] = Nil
  def wantsBigDecimal: Boolean = false
  def primitives: Set[Type] = Set(classOf[JValue], classOf[JObject], classOf[JArray])
  def companions: List[(Class[_], AnyRef)] = Nil
  def strict: Boolean = false 

  /**
   * The name of the field in JSON where type hints are added (jsonClass by default)
   */
  def typeHintFieldName: String = "jsonClass"

  /**
   * Parameter name reading strategy. By default 'paranamer' is used.
   */
  def parameterNameReader: reflect.ParameterNameReader = reflect.ParanamerReader

  def withBigDecimal: Formats = new Formats {
    val dateFormat: DateFormat = self.dateFormat
    override val typeHintFieldName: String = self.typeHintFieldName
    override val parameterNameReader: reflect.ParameterNameReader = self.parameterNameReader
    override val typeHints: TypeHints = self.typeHints
    override val customSerializers: List[Serializer[_]] = self.customSerializers
    override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = self.fieldSerializers
    override val wantsBigDecimal: Boolean = true
    override val primitives: Set[Type] = self.primitives
    override val companions: List[(Class[_], AnyRef)] = self.companions
    override val strict: Boolean = self.strict
  }

  def withDouble: Formats = new Formats {
    val dateFormat: DateFormat = self.dateFormat
    override val typeHintFieldName: String = self.typeHintFieldName
    override val parameterNameReader: reflect.ParameterNameReader = self.parameterNameReader
    override val typeHints: TypeHints = self.typeHints
    override val customSerializers: List[Serializer[_]] = self.customSerializers
    override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = self.fieldSerializers
    override val wantsBigDecimal: Boolean = false
    override val primitives: Set[Type] = self.primitives
    override val companions: List[(Class[_], AnyRef)] = self.companions
    override val strict: Boolean = self.strict
  }

  def withCompanions(comps: (Class[_], AnyRef)*): Formats = {
    new Formats {
      val dateFormat: DateFormat = self.dateFormat
      override val typeHintFieldName: String = self.typeHintFieldName
      override val parameterNameReader: reflect.ParameterNameReader = self.parameterNameReader
      override val typeHints: TypeHints = self.typeHints
      override val customSerializers: List[Serializer[_]] = self.customSerializers
      override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = self.fieldSerializers
      override val wantsBigDecimal: Boolean = self.wantsBigDecimal
      override val primitives: Set[Type] = self.primitives
      override val companions: List[(Class[_], AnyRef)] = comps.toList ::: self.companions
      override val strict: Boolean = self.strict
    }
  }

  /**
   * Adds the specified type hints to this formats.
   */
  def + (extraHints: TypeHints): Formats = new Formats {
    val dateFormat: DateFormat = self.dateFormat
    override val typeHintFieldName: String = self.typeHintFieldName
    override val parameterNameReader: reflect.ParameterNameReader = self.parameterNameReader
    override val typeHints: TypeHints = self.typeHints + extraHints
    override val customSerializers: List[Serializer[_]] = self.customSerializers
    override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = self.fieldSerializers
    override val wantsBigDecimal: Boolean = self.wantsBigDecimal
    override val primitives: Set[Type] = self.primitives
    override val companions: List[(Class[_], AnyRef)] = self.companions
    override val strict: Boolean = self.strict

  }

  /**
   * Adds the specified custom serializer to this formats.
   */
  def + (newSerializer: Serializer[_]): Formats = new Formats {
    val dateFormat: DateFormat = self.dateFormat
    override val typeHintFieldName: String = self.typeHintFieldName
    override val parameterNameReader: reflect.ParameterNameReader = self.parameterNameReader
    override val typeHints: TypeHints = self.typeHints
    override val customSerializers: List[Serializer[_]] = newSerializer :: self.customSerializers
    override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = self.fieldSerializers
    override val wantsBigDecimal: Boolean = self.wantsBigDecimal
    override val primitives: Set[Type] = self.primitives
    override val companions: List[(Class[_], AnyRef)] = self.companions
    override val strict: Boolean = self.strict
  }

  /**
   * Adds the specified custom serializers to this formats.
   */
  def ++ (newSerializers: Traversable[Serializer[_]]): Formats =
    newSerializers.foldLeft(this)(_ + _)

  /**
   * Adds a field serializer for a given type to this formats.
   */
  def + [A](newSerializer: FieldSerializer[A])(implicit mf: Manifest[A]): Formats = new Formats {
    val dateFormat: DateFormat = self.dateFormat
    override val typeHintFieldName: String = self.typeHintFieldName
    override val parameterNameReader: reflect.ParameterNameReader = self.parameterNameReader
    override val typeHints: TypeHints = self.typeHints
    override val customSerializers: List[Serializer[_]] = self.customSerializers
    override val fieldSerializers: List[(Class[_], FieldSerializer[_])] =
      (mf.erasure -> newSerializer) :: self.fieldSerializers
    override val wantsBigDecimal: Boolean = self.wantsBigDecimal
    override val primitives: Set[Type] = self.primitives
    override val companions: List[(Class[_], AnyRef)] = self.companions
    override val strict: Boolean = self.strict
  }

  private[json4s] def fieldSerializer(clazz: Class[_]): Option[FieldSerializer[_]] = {
    import ClassDelta._

    val ord = Ordering[Int].on[(Class[_], FieldSerializer[_])](x => delta(x._1, clazz))
    fieldSerializers filter (_._1.isAssignableFrom(clazz)) match {
      case Nil => None
      case xs  => Some((xs min ord)._2)
    }
  }

  def customSerializer(implicit format: Formats) =
    customSerializers.foldLeft(Map(): PartialFunction[Any, JValue]) { (acc, x) =>
      acc.orElse(x.serialize)
    }

  def customDeserializer(implicit format: Formats) =
    customSerializers.foldLeft(Map(): PartialFunction[(TypeInfo, JValue), Any]) { (acc, x) =>
      acc.orElse(x.deserialize)
    }
}

  trait Serializer[A] {
    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), A]
    def serialize(implicit format: Formats): PartialFunction[Any, JValue]
  }

  /** Type hints can be used to alter the default conversion rules when converting
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
   * implicit val formats = DefaultFormats.withHints(hints)
   * </pre>
   */
  trait TypeHints {
    import ClassDelta._

    val hints: List[Class[_]]

    /** Return hint for given type.
     */
    def hintFor(clazz: Class[_]): String

    /** Return type for given hint.
     */
    def classFor(hint: String): Option[Class[_]]

    @deprecated("Use `containsHint` without `_?` instead", "3.2.0")
    def containsHint_?(clazz: Class[_]): Boolean = containsHint(clazz)
    def containsHint(clazz: Class[_]): Boolean = hints exists (_ isAssignableFrom clazz)
    def deserialize: PartialFunction[(String, JObject), Any] = Map()
    def serialize: PartialFunction[Any, JObject] = Map()

    def components: List[TypeHints] = List(this)



    /**
     * Adds the specified type hints to this type hints.
     */
    def + (hints: TypeHints): TypeHints = CompositeTypeHints(hints.components ::: components)

    private[TypeHints] case class CompositeTypeHints(override val components: List[TypeHints]) extends TypeHints {
      val hints: List[Class[_]] = components.flatMap(_.hints)

      /**
       * Chooses most specific class.
       */
      def hintFor(clazz: Class[_]): String = {
        (components.reverse
          filter (_.containsHint(clazz))
          map (th => (th.hintFor(clazz), th.classFor(th.hintFor(clazz)).getOrElse(sys.error("hintFor/classFor not invertible for " + th))))
          sortWith ((x, y) => (delta(x._2, clazz) - delta(y._2, clazz)) <= 0)).head._1
      }

      def classFor(hint: String): Option[Class[_]] = {
        def hasClass(h: TypeHints) =
          scala.util.control.Exception.allCatch opt (h.classFor(hint)) map (_.isDefined) getOrElse(false)

        components find (hasClass) flatMap (_.classFor(hint))
    }

      override def deserialize: PartialFunction[(String, JObject), Any] = components.foldLeft[PartialFunction[(String, JObject),Any]](Map()) {
        (result, cur) => result.orElse(cur.deserialize)
      }

      override def serialize: PartialFunction[Any, JObject] = components.foldLeft[PartialFunction[Any, JObject]](Map()) {
        (result, cur) => result.orElse(cur.serialize)
      }
    }
  }

  private[json4s] object ClassDelta {
    def delta(class1: Class[_], class2: Class[_]): Int = {
      if (class1 == class2) 0
      else if (class1.getInterfaces.contains(class2)) 0
      else if (class2.getInterfaces.contains(class1)) 0
      else if (class1.isAssignableFrom(class2)) {
        1 + delta(class1, class2.getSuperclass)
      }
      else if (class2.isAssignableFrom(class1)) {
        1 + delta(class1.getSuperclass, class2)
      }
      else sys.error("Don't call delta unless one class is assignable from the other")
    }
  }

  /** Do not use any type hints.
   */
  case object NoTypeHints extends TypeHints {
    val hints: List[Class[_]] = Nil
    def hintFor(clazz: Class[_]) = sys.error("NoTypeHints does not provide any type hints.")
    def classFor(hint: String) = None
  }

  /** Use short class name as a type hint.
   */
  case class ShortTypeHints(hints: List[Class[_]]) extends TypeHints {
    def hintFor(clazz: Class[_]) = clazz.getName.substring(clazz.getName.lastIndexOf(".")+1)
    def classFor(hint: String) = hints find (hintFor(_) == hint)
  }

  /** Use full class name as a type hint.
   */
  case class FullTypeHints(hints: List[Class[_]]) extends TypeHints {
    def hintFor(clazz: Class[_]) = clazz.getName
    def classFor(hint: String) = {
      Reflector.scalaTypeOf(hint).map(_.erasure)//.find(h => hints.exists(l => l.isAssignableFrom(h.erasure)))
    }
  }

  /** Default date format is UTC time.
   */
  object DefaultFormats extends DefaultFormats {
    val losslessDate = new ThreadLocal(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
    val UTC = TimeZone.getTimeZone("UTC")

  }

  private[json4s] class ThreadLocal[A](init: => A) extends java.lang.ThreadLocal[A] with (() => A) {
    override def initialValue = init
    def apply = get
  }
  trait DefaultFormats extends Formats {
    import java.text.{ParseException, SimpleDateFormat}

    private[this] val df = new ThreadLocal[SimpleDateFormat](dateFormatter)

    override val typeHintFieldName: String = "jsonClass"
    override val parameterNameReader: reflect.ParameterNameReader = reflect.ParanamerReader
    override val typeHints: TypeHints = NoTypeHints
    override val customSerializers: List[Serializer[_]] = Nil
    override val fieldSerializers: List[(Class[_], FieldSerializer[_])] = Nil
    override val wantsBigDecimal: Boolean = false
    override val primitives: Set[Type] = Set(classOf[JValue], classOf[JObject], classOf[JArray])
    override val companions: List[(Class[_], AnyRef)] = Nil
    override val strict: Boolean = false

    val dateFormat: DateFormat = new DateFormat {
      def parse(s: String) = try {
        Some(formatter.parse(s))
      } catch {
        case e: ParseException => None
      }

      def format(d: Date) = formatter.format(d)

      private[this] def formatter = {
        val f = df()
        f.setTimeZone(DefaultFormats.UTC)
        f
      }
    }

    protected def dateFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /** Lossless date format includes milliseconds too.
     */
    def lossless: Formats = new DefaultFormats {
      override def dateFormatter = DefaultFormats.losslessDate()
    }

    /** Default formats with given <code>TypeHint</code>s.
     */
    def withHints(hints: TypeHints): Formats = new DefaultFormats {
      override val typeHints = hints
    }
  }



  class CustomSerializer[A: Manifest](
    ser: Formats => (PartialFunction[JValue, A], PartialFunction[Any, JValue])) extends Serializer[A] {

    val Class = implicitly[Manifest[A]].erasure

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Class, _), json) =>
        if (ser(format)._1.isDefinedAt(json)) ser(format)._1(json)
        else throw new MappingException("Can't convert " + json + " to " + Class)
    }

    def serialize(implicit format: Formats) = ser(format)._2
  }
