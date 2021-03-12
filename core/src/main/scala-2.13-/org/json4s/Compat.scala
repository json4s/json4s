package org.json4s

import scala.language.reflectiveCalls

private[json4s] object Compat {
  def makeCollection(clazz: Class[_], array: Array[_]): Option[Any] = {
    reflect.ScalaSigReader.companions(clazz.getName).flatMap(_._2).map {
      _.asInstanceOf[{ def apply(elems: collection.Seq[_]): Any }].apply(array.toSeq)
    }
  }
}
