package org.json4s

import scala.collection.immutable
import scala.annotation.implicitNotFound

@implicitNotFound(
  "No JSON serializer found for type ${T}. Try to implement an implicit Writer or JsonFormat for this type."
)
trait Writer[-T] {
  def write(obj: T): JValue
}

object Writer {
  def apply[A](implicit a: Writer[A]): Writer[A] = a
}

trait DefaultWriters {

  protected abstract class W[-T](fn: T => JValue) extends Writer[T] {
    def write(obj: T): JValue = fn(obj)
  }

  implicit object IntWriter extends W[Int](JInt(_))
  implicit object ByteWriter extends W[Byte](x => JInt(x: Long))
  implicit object ShortWriter extends W[Short](x => JInt(x: Long))
  implicit object LongWriter extends W[Long](JInt(_))
  implicit object BigIntWriter extends W[BigInt](JInt(_))
  implicit object BooleanWriter extends W[Boolean](JBool(_))
  implicit object StringWriter extends W[String](JString(_))
  implicit def arrayWriter[T](implicit valueWriter: Writer[T]): Writer[Array[T]] = new Writer[Array[T]] {
    def write(obj: Array[T]): JValue = JArray(obj.map(valueWriter.write(_)).toList)
  }
  implicit def mapWriter[V](implicit valueWriter: Writer[V]): Writer[immutable.Map[String, V]] =
    new Writer[Map[String, V]] {
      def write(obj: Map[String, V]): JValue = JObject(
        obj.map({ case (k, v) => k -> valueWriter.write(v) }).toList
      )
    }
  implicit object JValueWriter extends W[JValue](identity)
  implicit def OptionWriter[T](implicit valueWriter: Writer[T]): Writer[Option[T]] = new Writer[Option[T]] {
    def write(obj: Option[T]): JValue = obj match {
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
