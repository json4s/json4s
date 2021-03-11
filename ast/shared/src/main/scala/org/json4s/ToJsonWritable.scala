package org.json4s

class ToJsonWritable[T](private val a: T) extends AnyVal {
  def asJValue(implicit writer: Writer[T]): JValue = writer.write(a)
}
