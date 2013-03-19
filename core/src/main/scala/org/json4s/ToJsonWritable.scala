package org.json4s

class ToJsonWritable[T](a: T)(implicit writer: Writer[T]) {
  def asJValue = writer.write(a)
}