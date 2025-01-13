package org.json4s

private[json4s] trait DefaultReaders0 {
  implicit def iterableReader[F[_], V](implicit
      f: scala.collection.Factory[V, F[V]],
      valueReader: Reader[V]
  ): Reader[F[V]] =
    Reader.from[F[V]] {
      case JArray(items) =>
        val rights = f.newBuilder
        val lefts = List.newBuilder[MappingException]
        items.foreach { v =>
          valueReader.readEither(v) match {
            case Right(a) =>
              rights += a
            case Left(a) =>
              lefts += a
          }
        }
        val l = lefts.result()
        if (l.isEmpty) {
          Right(rights.result())
        } else {
          Left(new MappingException.Multi(l))
        }
      case x =>
        Left(new MappingException(s"Can't convert ${x} to Iterable."))
    }
}
