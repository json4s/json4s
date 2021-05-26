package org.json4s

trait AsJsonInput[A] { self =>
  def toJsonInput(a: A): JsonInput
  def contramap[B](f: B => A): AsJsonInput[B] =
    new AsJsonInput[B] {
      def toJsonInput(a: B): JsonInput = self.toJsonInput(f(a))
    }
}

object AsJsonInput extends AsJsonInputInstances {
  def apply[A](implicit a: AsJsonInput[A]): AsJsonInput[A] = a
  def asJsonInput[A](input: A)(implicit a: AsJsonInput[A]): JsonInput = a.toJsonInput(input)

  def fromFunction[A](f: A => JsonInput): AsJsonInput[A] =
    new AsJsonInput[A] {
      def toJsonInput(a: A): JsonInput = f(a)
    }

  implicit val stringAsJsonInput: AsJsonInput[String] =
    x => StringInput(x)

  implicit def readerAsJsonInput[A <: java.io.Reader]: AsJsonInput[A] =
    x => ReaderInput(x)

  implicit def streamAsJsonInput[A <: java.io.InputStream]: AsJsonInput[A] =
    x => StreamInput(x)

  implicit val identity: AsJsonInput[JsonInput] =
    x => x
}
