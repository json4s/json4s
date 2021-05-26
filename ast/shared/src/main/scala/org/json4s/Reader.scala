package org.json4s

import scala.collection.immutable
import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

@implicitNotFound(
  "No JSON deserializer found for type ${T}. Try to implement an implicit Reader or JsonFormat for this type."
)
trait Reader[T] { self =>
  def readEither(value: JValue): Either[MappingException, T]

  def map[A](f: T => A): Reader[A] =
    new Reader[A] {
      def readEither(value: JValue): Either[MappingException, A] =
        self.readEither(value).map(f)
    }
}

object Reader extends ReaderFunctions {
  def apply[A](implicit a: Reader[A]): Reader[A] = a
  def from[A](f: JValue => Either[MappingException, A]): Reader[A] =
    new Reader[A] {
      override def readEither(value: JValue): Either[MappingException, A] =
        f(value)
    }

  def fromPartialFunction[A](f: PartialFunction[JValue, A])(error: JValue => MappingException): Reader[A] =
    new Reader[A] {
      override def readEither(value: JValue): Either[MappingException, A] =
        if (f.isDefinedAt(value)) {
          Right(f(value))
        } else {
          Left(error(value))
        }
    }
}

object DefaultReaders extends DefaultReaders
trait DefaultReaders extends DefaultReaders0 {
  implicit val IntReader: Reader[Int] = Reader.fromPartialFunction[Int] {
    case JInt(x) => x.intValue
    case JLong(x) => x.intValue
    case JDouble(x) => x.intValue
    case JDecimal(x) => x.intValue
  }(x => new MappingException(s"Can't convert ${x} to Int."))

  implicit val BigIntReader: Reader[BigInt] = Reader.fromPartialFunction[BigInt] {
    case JInt(x) => x
    case JLong(x) => BigInt(x)
    case JDouble(x) => BigInt(x.longValue)
    case JDecimal(x) => x.toBigInt
  }(x => new MappingException(s"Can't convert ${x} to BigInt."))

  implicit val LongReader: Reader[Long] = Reader.fromPartialFunction[Long] {
    case JInt(x) => x.longValue
    case JLong(x) => x
    case JDouble(x) => x.longValue
    case JDecimal(x) => x.longValue
  }(x => new MappingException(s"Can't convert ${x} to Long."))

  implicit val ShortReader: Reader[Short] = Reader.fromPartialFunction[Short] {
    case JInt(x) => x.shortValue
    case JLong(x) => x.shortValue
    case JDouble(x) => x.shortValue
    case JDecimal(x) => x.shortValue
    case JNull => 0
  }(x => new MappingException(s"Can't convert ${x} to Short."))

  implicit val ByteReader: Reader[Byte] = Reader.fromPartialFunction[Byte] {
    case JInt(x) => x.byteValue
    case JLong(x) => x.byteValue
    case JDouble(x) => x.byteValue
    case JDecimal(x) => x.byteValue
    case JNull => 0
  }(x => new MappingException(s"Can't convert ${x} to Byte."))

  implicit val FloatReader: Reader[Float] = Reader.fromPartialFunction[Float] {
    case JInt(x) => x.floatValue
    case JLong(x) => x.floatValue
    case JDouble(x) => x.floatValue
    case JDecimal(x) => x.floatValue
    case JNull => 0
  }(x => new MappingException(s"Can't convert ${x} to Float."))

  implicit val DoubleReader: Reader[Double] = Reader.fromPartialFunction[Double] {
    case JInt(x) => x.doubleValue
    case JLong(x) => x.doubleValue
    case JDouble(x) => x
    case JDecimal(x) => x.doubleValue
    case JNull => 0
  }(x => new MappingException(s"Can't convert ${x} to Double."))

  implicit val BigDecimalReader: Reader[BigDecimal] = Reader.fromPartialFunction[BigDecimal] {
    case JInt(x) => BigDecimal(x)
    case JLong(x) => BigDecimal(x)
    case JDouble(x) => BigDecimal(x)
    case JDecimal(x) => x
    case JNull => 0
  }(x => new MappingException(s"Can't convert ${x} to BigDecimal."))

  implicit val BooleanReader: Reader[Boolean] = Reader.fromPartialFunction[Boolean] {
    case JBool(v) => v
    case JNull => false
  }(x => new MappingException(s"Can't convert ${x} to Boolean."))

  implicit val StringReader: Reader[String] = Reader.fromPartialFunction[String] {
    case JInt(x) => x.toString
    case JLong(x) => x.toString
    case JDecimal(x) => x.toString
    case JDouble(x) => x.toString
    case JBool(x) => x.toString
    case JString(s) => s
    case JNull => null
  }(x => new MappingException(s"Can't convert ${x} to String."))

  implicit def mapReader[V](implicit valueReader: Reader[V]): Reader[immutable.Map[String, V]] =
    Reader.from[immutable.Map[String, V]] {
      case JObject(values) =>
        val rights = Map.newBuilder[String, V]
        val lefts = List.newBuilder[MappingException]
        values.foreach { case JField(k, v) =>
          valueReader.readEither(v) match {
            case Right(a) =>
              rights += ((k, a))
            case Left(a) =>
              lefts += a
          }
        }
        val l = lefts.result()
        if (l.isEmpty) {
          Right(rights.result())
        } else {
          Left(new MappingException.Multi(l))
        }
      case x =>
        Left(new MappingException(s"Can't convert ${x} to Map."))
    }

  implicit def arrayReader[T: ClassTag: Reader]: Reader[Array[T]] =
    Reader[List[T]].map(_.toArray)

  implicit val JValueReader: Reader[JValue] = new Reader[JValue] {
    def read(value: JValue): JValue = value
    def readEither(value: JValue) = Right(value)
  }

  implicit val JObjectReader: Reader[JObject] = Reader.from[JObject] {
    case x: JObject =>
      Right(x)
    case x =>
      Left(new MappingException(s"JObject expected, but got ${x}."))
  }

  implicit val JArrayReader: Reader[JArray] = Reader.from[JArray] {
    case x: JArray =>
      Right(x)
    case x =>
      Left(new MappingException(s"JArray expected, but got ${x}."))
  }

  implicit def OptionReader[T](implicit valueReader: Reader[T]): Reader[Option[T]] = new Reader[Option[T]] {
    def readEither(value: JValue) =
      valueReader.readEither(value) match {
        case Right(x) => Right(Some(x))
        case Left(x) => Right(None)
      }
  }
}
