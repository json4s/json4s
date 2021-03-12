package org.json4s

import collection.immutable
import annotation.implicitNotFound
import scala.reflect.ClassTag

// based on the type classes from play 2 but with the conversions from lift-json
@implicitNotFound(
  "No JSON deserializer found for type ${T}. Try to implement an implicit Reader or JsonFormat for this type."
)
trait Reader[T] {
  def read(value: JValue): T
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
    def read(value: _root_.org.json4s.JValue): Byte = value match {
      case JInt(x) => x.byteValue
      case JLong(x) => x.byteValue
      case JDouble(x) => x.byteValue
      case JDecimal(x) => x.byteValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Byte.")
    }
  }

  implicit object FloatReader extends Reader[Float] {
    def read(value: _root_.org.json4s.JValue): Float = value match {
      case JInt(x) => x.floatValue
      case JLong(x) => x.floatValue
      case JDouble(x) => x.floatValue
      case JDecimal(x) => x.floatValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Float.")
    }
  }

  implicit object DoubleReader extends Reader[Double] {
    def read(value: _root_.org.json4s.JValue): Double = value match {
      case JInt(x) => x.doubleValue
      case JLong(x) => x.doubleValue
      case JDouble(x) => x
      case JDecimal(x) => x.doubleValue
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to Double.")
    }
  }

  implicit object BigDecimalReader extends Reader[BigDecimal] {
    def read(value: _root_.org.json4s.JValue): BigDecimal = value match {
      case JInt(x) => BigDecimal(x)
      case JLong(x) => BigDecimal(x)
      case JDouble(x) => BigDecimal(x)
      case JDecimal(x) => x
      case JNull => 0
      case x => throw new MappingException(s"Can't convert ${x} to BigDecimal.")
    }
  }

  implicit object BooleanReader extends Reader[Boolean] {
    def read(value: _root_.org.json4s.JValue): Boolean = value match {
      case JBool(v) => v
      case JNull => false
      case x => throw new MappingException(s"Can't convert ${x} to Boolean.")
    }
  }

  implicit object StringReader extends Reader[String] {
    def read(value: _root_.org.json4s.JValue): String = value match {
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
      def read(value: _root_.org.json4s.JValue): Map[String, V] = value match {
        case JObject(v) => Map(v.map({ case JField(k, vl) => k -> valueReader.read(vl) }): _*)
        case x => throw new MappingException(s"Can't convert ${x} to Map.")
      }
    }

  implicit def arrayReader[T: ClassTag: Reader]: Reader[Array[T]] = new Reader[Array[T]] {
    def read(value: _root_.org.json4s.JValue): Array[T] = {
      value.as[List[T]].toArray
    }
  }

  implicit object JValueReader extends Reader[JValue] {
    def read(value: _root_.org.json4s.JValue): _root_.org.json4s.JValue = value
  }

  implicit object JObjectReader extends Reader[JObject] {
    def read(value: _root_.org.json4s.JValue): _root_.org.json4s.JObject = value match {
      case x: JObject => x
      case x => throw new MappingException(s"JObject expected, but got ${x}.")
    }
  }

  implicit object JArrayReader extends Reader[JArray] {
    def read(value: _root_.org.json4s.JValue): _root_.org.json4s.JArray = value match {
      case x: JArray => x
      case x => throw new MappingException(s"JArray expected, but got ${x}.")
    }
  }

  implicit def OptionReader[T](implicit valueReader: Reader[T]): Reader[Option[T]] = new Reader[Option[T]] {
    def read(value: _root_.org.json4s.JValue): Option[T] = {
      import scala.util.control.Exception.catching
      catching(classOf[RuntimeException], classOf[MappingException]) opt { valueReader read value }
    }
  }
}

@implicitNotFound(
  "No JSON serializer found for type ${T}. Try to implement an implicit Writer or JsonFormat for this type."
)
trait Writer[-T] {
  def write(obj: T): JValue
}
trait DefaultWriters {

  protected abstract class W[-T](fn: T => JValue) extends Writer[T] {
    def write(obj: T): _root_.org.json4s.JValue = fn(obj)
  }

  implicit object IntWriter extends W[Int](JInt(_))
  implicit object ByteWriter extends W[Byte](x => JInt(x: Long))
  implicit object ShortWriter extends W[Short](x => JInt(x: Long))
  implicit object LongWriter extends W[Long](JInt(_))
  implicit object BigIntWriter extends W[BigInt](JInt(_))
  implicit object BooleanWriter extends W[Boolean](JBool(_))
  implicit object StringWriter extends W[String](JString(_))
  implicit def arrayWriter[T](implicit valueWriter: Writer[T]): Writer[Array[T]] = new Writer[Array[T]] {
    def write(obj: Array[T]): _root_.org.json4s.JValue = JArray(obj.map(valueWriter.write(_)).toList)
  }
  implicit def mapWriter[V](implicit valueWriter: Writer[V]): Writer[immutable.Map[String, V]] =
    new Writer[Map[String, V]] {
      def write(obj: Map[String, V]): _root_.org.json4s.JValue = JObject(
        obj.map({ case (k, v) => k -> valueWriter.write(v) }).toList
      )
    }
  implicit object JValueWriter extends W[JValue](identity)
  implicit def OptionWriter[T](implicit valueWriter: Writer[T]): Writer[Option[T]] = new Writer[Option[T]] {
    def write(obj: Option[T]): _root_.org.json4s.JValue = obj match {
      case Some(v) => valueWriter.write(v)
      case _ => JNull
    }
  }
}

trait DoubleWriters extends DefaultWriters {
  implicit object FloatWriter extends W[Float](x => JDouble(x: Double))
  implicit object DoubleWriter extends W[Double](JDouble(_))
  implicit object BigDecimalWriter extends W[BigDecimal](d => JDouble(d.doubleValue))
}

trait BigDecimalWriters extends DefaultWriters {
  implicit object FloatWriter extends W[Float](x => JDecimal(x: Double))
  implicit object DoubleWriter extends W[Double](JDecimal(_))
  implicit object BigDecimalWriter extends W[BigDecimal](d => JDecimal(d))
}

object BigDecimalWriters extends BigDecimalWriters
object DoubleWriters extends DoubleWriters
object DefaultWriters extends DoubleWriters // alias for DoubleWriters

@implicitNotFound(
  "No Json formatter found for type ${T}. Try to implement an implicit JsonFormat for this type."
)
trait JsonFormat[T] extends Writer[T] with Reader[T]
trait BigDecimalJsonFormats extends DefaultJsonFormats with DefaultReaders with BigDecimalWriters
trait DoubleJsonFormats extends DefaultJsonFormats with DefaultReaders with DoubleWriters
object BigDecimalJsonFormats extends BigDecimalJsonFormats
object DoubleJsonFormats extends DoubleJsonFormats
object DefaultJsonFormats extends DoubleJsonFormats

trait DefaultJsonFormats {

  implicit def GenericFormat[T](implicit reader: Reader[T], writer: Writer[T]): JsonFormat[T] = new JsonFormat[T] {
    def write(obj: T): _root_.org.json4s.JValue = writer.write(obj)
    def read(value: _root_.org.json4s.JValue): T = reader.read(value)
  }
}
