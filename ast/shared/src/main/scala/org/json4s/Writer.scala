package org.json4s

import scala.collection.immutable
import scala.annotation.implicitNotFound

@implicitNotFound(
  "No JSON serializer found for type ${T}. Try to implement an implicit Writer or JsonFormat for this type."
)
trait Writer[-T] {
  def write(obj: T): JValue
}

object Writer extends WriterFunctions {
  def apply[A](implicit a: Writer[A]): Writer[A] = a
}

trait DefaultWriters {

  protected[this] class W[-T](fn: T => JValue) extends Writer[T] {
    def write(obj: T): JValue = fn(obj)
  }

  implicit val IntWriter: Writer[Int] = new W[Int](JInt(_))
  implicit val ByteWriter: Writer[Byte] = new W[Byte](x => JInt(x: Long))
  implicit val ShortWriter: Writer[Short] = new W[Short](x => JInt(x: Long))
  implicit val LongWriter: Writer[Long] = new W[Long](JInt(_))
  implicit val BigIntWriter: Writer[BigInt] = new W[BigInt](JInt(_))
  implicit val BooleanWriter: Writer[Boolean] = new W[Boolean](JBool(_))
  implicit val StringWriter: Writer[String] = new W[String](JString(_))
  implicit def arrayWriter[T](implicit valueWriter: Writer[T]): Writer[Array[T]] = new Writer[Array[T]] {
    def write(obj: Array[T]): JValue = JArray(obj.map(valueWriter.write(_)).toList)
  }
  implicit def seqWriter[T: Writer]: Writer[collection.Seq[T]] = new Writer[collection.Seq[T]] {
    def write(a: collection.Seq[T]) = JArray(a.map(Writer[T].write(_)).toList)
  }
  implicit def mapWriter[V](implicit valueWriter: Writer[V]): Writer[immutable.Map[String, V]] =
    new Writer[Map[String, V]] {
      def write(obj: Map[String, V]): JValue = JObject(
        obj.map({ case (k, v) => k -> valueWriter.write(v) }).toList
      )
    }
  implicit val JValueWriter: Writer[JValue] = new W[JValue](identity)
  implicit def OptionWriter[T](implicit valueWriter: Writer[T]): Writer[Option[T]] = new Writer[Option[T]] {
    def write(obj: Option[T]): JValue = obj match {
      case Some(v) => valueWriter.write(v)
      case _ => JNull
    }
  }
}

trait DoubleWriters extends DefaultWriters {
  implicit val FloatWriter: Writer[Float] = new W[Float](x => JDouble(x: Double))
  implicit val DoubleWriter: Writer[Double] = new W[Double](JDouble(_))
  implicit val BigDecimalWriter: Writer[BigDecimal] = new W[BigDecimal](d => JDouble(d.doubleValue))
}

trait BigDecimalWriters extends DefaultWriters {
  implicit val FloatWriter: Writer[Float] = new W[Float](x => JDecimal(x: Double))
  implicit val DoubleWriter: Writer[Double] = new W[Double](JDecimal(_))
  implicit val BigDecimalWriter: Writer[BigDecimal] = new W[BigDecimal](d => JDecimal(d))
}

object BigDecimalWriters extends BigDecimalWriters
object DoubleWriters extends DoubleWriters
object DefaultWriters extends DoubleWriters // alias for DoubleWriters
