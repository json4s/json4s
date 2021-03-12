package org.json4s.ext

import org.json4s._
import scala.reflect.{ClassTag, classTag}

trait ClassType[A, B] {
  def unwrap(b: B)(implicit format: Formats): A
  def wrap(a: A)(implicit format: Formats): B
}

case class ClassSerializer[A: ClassTag, B: Manifest](t: ClassType[A, B]) extends Serializer[A] {
  private[this] val Class = classTag[A].runtimeClass

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), A] = {
    case (TypeInfo(Class, _), json) =>
      json match {
        case JNull => null.asInstanceOf[A]
        case xs: JObject if xs.extractOpt[B].isDefined => t.unwrap(xs.extract[B])
        case value => throw new MappingException(s"Can't convert $value to $Class")
      }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case a: A if a.asInstanceOf[AnyRef].getClass == Class => Extraction.decompose(t.wrap(a))
  }
}
