package org.json4s

trait JsonKeyWriter[A] { self =>
  def write(key: A): String

  def contramap[B](f: B => A): JsonKeyWriter[B] =
    (key: B) => self.write(f(key))
}

object JsonKeyWriter {
  def apply[A](implicit a: JsonKeyWriter[A]): JsonKeyWriter[A] = a

  def of[A](f: A => String): JsonKeyWriter[A] =
    (key: A) => f(key)

  def fromToString[A]: JsonKeyWriter[A] =
    of[A](_.toString)

  implicit val string: JsonKeyWriter[String] = of[String](x => x)
}
