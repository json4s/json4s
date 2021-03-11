package org.json4s

private[json4s] trait DefaultReaders0 {
  implicit def iterableReader[F[_], V](implicit
    f: scala.collection.Factory[V, F[V]],
    valueReader: Reader[V]
  ): Reader[F[V]] =
    new Reader[F[V]] {
      def read(value: JValue): F[V] = value match {
        case JArray(items) =>
          val builder = f.newBuilder
          items.foldLeft(builder) { (acc, i) => acc += valueReader.read(i); acc }.result()
        case x =>
          throw new MappingException(s"Can't convert ${x} to Iterable.")
      }
    }
}
