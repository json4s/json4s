package org.json4s

import scala.annotation.implicitNotFound
import scala.collection.immutable

@implicitNotFound(
  "No JSON serializer found for type ${T}. Try to implement an implicit Writer or JsonFormat for this type."
)
trait Writer[-T] { self =>
  def write(obj: T): JValue

  def contramap[A](f: A => T): Writer[A] =
    (obj: A) => self.write(f(obj))
}

object Writer extends WriterFunctions {
  def apply[A](implicit a: Writer[A]): Writer[A] = a
}

trait DefaultWriters {
  implicit val IntWriter: Writer[Int] = x => JInt(x)
  implicit val ByteWriter: Writer[Byte] = (x => JInt(x: Long))
  implicit val ShortWriter: Writer[Short] = (x => JInt(x: Long))
  implicit val LongWriter: Writer[Long] = JInt(_)
  implicit val BigIntWriter: Writer[BigInt] = JInt(_)
  implicit val BooleanWriter: Writer[Boolean] = JBool(_)
  implicit val StringWriter: Writer[String] = JString(_)
  implicit def arrayWriter[T](implicit valueWriter: Writer[T]): Writer[Array[T]] = (obj: Array[T]) =>
    JArray(obj.map(valueWriter.write(_)).toList)
  implicit def seqWriter[T: Writer]: Writer[collection.Seq[T]] = (a: collection.Seq[T]) =>
    JArray(a.map(Writer[T].write(_)).toList)
  implicit def mapWriter[K, V](implicit
    keyWriter: JsonKeyWriter[K],
    valueWriter: Writer[V]
  ): Writer[immutable.Map[K, V]] =
    (obj: Map[K, V]) =>
      JObject(
        obj.map { case (k, v) => keyWriter.write(k) -> valueWriter.write(v) }.toList
      )
  implicit val JValueWriter: Writer[JValue] = x => x
  implicit def OptionWriter[T](implicit valueWriter: Writer[T]): Writer[Option[T]] = (obj: Option[T]) =>
    obj match {
      case Some(v) => valueWriter.write(v)
      case _ => JNothing
    }
}

trait DoubleWriters extends DefaultWriters {
  implicit val FloatWriter: Writer[Float] = x => JDouble(x: Double)
  implicit val DoubleWriter: Writer[Double] = JDouble(_)
  implicit val BigDecimalWriter: Writer[BigDecimal] = d => JDouble(d.doubleValue)
}

trait BigDecimalWriters extends DefaultWriters {
  implicit val FloatWriter: Writer[Float] = x => JDecimal(x: Double)
  implicit val DoubleWriter: Writer[Double] = JDecimal(_)
  implicit val BigDecimalWriter: Writer[BigDecimal] = d => JDecimal(d)
}

object BigDecimalWriters extends BigDecimalWriters
object DoubleWriters extends DoubleWriters
object DefaultWriters extends DoubleWriters // alias for DoubleWriters
