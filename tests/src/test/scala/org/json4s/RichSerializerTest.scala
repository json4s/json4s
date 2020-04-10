package org.json4s

import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.reflect.ScalaType
import org.json4s.Extraction._
import org.specs2.mutable.Specification

class RichSerializerTest extends Specification {

  object CustomTuple2Serializer extends RichSerializer[(_, _)] {

    override def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), (_, _)] = {
      case (scalaType, JArray(arr)) if classOf[(_, _)].isAssignableFrom(scalaType.erasure) =>
        require(arr.size == 2)
        (extract(arr.head, scalaType.typeArgs.head), extract(arr(1), scalaType.typeArgs(1)))
    }

    override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case (x, y) => JArray(List(decompose(x), decompose(y)))
    }
  }

  object TypeBearerDeserializer extends RichSerializer[TypeBearer[_]] {
    override def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), TypeBearer[_]] = {
      case (scalaType, obj: JObject) if scalaType.erasure == classOf[TypeBearer[_]] =>
        obj \ "name" match {
          case JString(s) => TypeBearer(s)(scalaType.typeArgs.head.manifest)
          case v: JValue => throw new MappingException(s"Wrong json type in $v")
        }
    }

    override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = ???
  }

  "it" should {
    "deserialize types which have type params" in {
      implicit val formats: Formats = DefaultFormats + CustomTuple2Serializer + TypeBearerDeserializer
      val json = """[{"name": "foo"}, {"name": "bar"}]"""
      val extracted = JsonMethods.parse(json).extract[(TypeBearer[TypeBearer[String]], TypeBearer[Int])]
      extracted._2.enclosedType shouldEqual manifest[Int]
      extracted._1.enclosedType shouldEqual manifest[TypeBearer[String]]
    }
  }

}

case class TypeBearer[T: Manifest](name: String) {
  def enclosedType: Manifest[T] = manifest[T]
}

