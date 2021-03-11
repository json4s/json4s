package org.json4s

import scala.collection.immutable
import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

@implicitNotFound(
  "No JSON deserializer found for type ${T}. Try to implement an implicit Reader or JsonFormat for this type."
)
trait Reader[T] {
  def read(value: JValue): T
}

object Reader {
  def apply[A](implicit a: Reader[A]): Reader[A] = a
}

object DefaultReaders extends DefaultReaders
trait DefaultReaders extends DefaultReaders0 {
  implicit object IntReader extends Reader[Int] {
    def read(value: JValue): Int = value match {
      case JInt(x) => x.intValue
      case JLong(x) => x.intValue
      case JDouble(x) => x.intValue
      case JDecimal(x) => x.intValue
      case x => throw new MappingException(s"Can't convert ${x} to Int.")
    }
  }

  implicit object BigIntReader extends Reader[BigInt] {
    def read(value: JValue): BigInt = value match {
      case JInt(x) => x
      case JLong(x) => BigInt(x)
      case JDouble(x) => BigInt(x.longValue)
      case JDecimal(x) => x.toBigInt
      case x => throw new MappingException(s"Can't convert ${x} to BigInt.")
    }
  }

  implicit object LongReader extends Reader[Long] {
    def read(value: JValue): Long = value match {
      case JInt(x) => x.longValue
      case JLong(x) => x
      case JDouble(x) => x.longValue
      case JDecimal(x) => x.longValue
      case x => throw new MappingException(s"Can't convert ${x} to Long.")
    }
  }

  implicit object ShortReader extends Reader[Short] {
    def read(value: JValue): Short = value match {
      case JInt(x) => x.shortValue
      case JLong(x) => x.shortValue
      case JDouble(x) => x.shortValue
      case JDecimal(x) => x.shortValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Short.")
    }
  }

  implicit object ByteReader extends Reader[Byte] {
    def read(value: JValue): Byte = value match {
      case JInt(x) => x.byteValue
      case JLong(x) => x.byteValue
      case JDouble(x) => x.byteValue
      case JDecimal(x) => x.byteValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Byte.")
    }
  }

  implicit object FloatReader extends Reader[Float] {
    def read(value: JValue): Float = value match {
      case JInt(x) => x.floatValue
      case JLong(x) => x.floatValue
      case JDouble(x) => x.floatValue
      case JDecimal(x) => x.floatValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Float.")
    }
  }

  implicit object DoubleReader extends Reader[Double] {
    def read(value: JValue): Double = value match {
      case JInt(x) => x.doubleValue
      case JLong(x) => x.doubleValue
      case JDouble(x) => x
      case JDecimal(x) => x.doubleValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Double.")
    }
  }

  implicit object BigDecimalReader extends Reader[BigDecimal] {
    def read(value: JValue): BigDecimal = value match {
      case JInt(x) => BigDecimal(x)
      case JLong(x) => BigDecimal(x)
      case JDouble(x) => BigDecimal(x)
      case JDecimal(x) => x
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to BigDecimal.")
    }
  }

  implicit object BooleanReader extends Reader[Boolean] {
    def read(value: JValue): Boolean = value match {
      case JBool(v) => v
      case JNull => false
      case x => throw new MappingException(s"Can't convert ${x} to Boolean.")
    }
  }

  implicit object StringReader extends Reader[String] {
    def read(value: JValue): String = value match {
      case JInt(x) => x.toString
      case JLong(x) => x.toString
      case JDecimal(x) => x.toString
      case JDouble(x) => x.toString
      case JBool(x) => x.toString
      case JString(s) => s
      case JNull => null
      case x => throw new MappingException(s"Can't convert ${x} to String.")
    }
  }

  implicit def mapReader[V](implicit valueReader: Reader[V]): Reader[immutable.Map[String, V]] =
    new Reader[immutable.Map[String, V]] {
      def read(value: JValue): Map[String, V] = value match {
        case JObject(v) => Map(v.map({ case JField(k, vl) => k -> valueReader.read(vl) }): _*)
        case x => throw new MappingException(s"Can't convert ${x} to Map.")
      }
    }

  implicit def arrayReader[T: ClassTag: Reader]: Reader[Array[T]] = new Reader[Array[T]] {
    def read(value: JValue): Array[T] = {
      Reader[List[T]].read(value).toArray
    }
  }

  implicit object JValueReader extends Reader[JValue] {
    def read(value: JValue): JValue = value
  }

  implicit object JObjectReader extends Reader[JObject] {
    def read(value: JValue): JObject = value match {
      case x: JObject => x
      case x => throw new MappingException(s"JObject expected, but got ${x}.")
    }
  }

  implicit object JArrayReader extends Reader[JArray] {
    def read(value: JValue): JArray = value match {
      case x: JArray => x
      case x => throw new MappingException(s"JArray expected, but got ${x}.")
    }
  }

  implicit def OptionReader[T](implicit valueReader: Reader[T]): Reader[Option[T]] = new Reader[Option[T]] {
    def read(value: JValue): Option[T] = {
      import scala.util.control.Exception.catching
      catching(classOf[RuntimeException], classOf[MappingException]) opt { valueReader read value }
    }
  }
}
