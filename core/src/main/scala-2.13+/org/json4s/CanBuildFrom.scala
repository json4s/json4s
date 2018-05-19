package org.json4s

import scala.annotation.implicitNotFound

@implicitNotFound(msg = "Cannot construct a collection of type ${To} with elements of type ${Elem} based on a collection of type ${From}.")
trait CanBuildFrom[-From, -Elem, +To] {
  def apply(): scala.collection.mutable.Builder[Elem, To]
}
