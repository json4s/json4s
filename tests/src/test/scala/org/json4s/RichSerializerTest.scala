package org.json4s

import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods
import org.json4s.reflect.ScalaType
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable.HashMap

class RichSerializerTest extends AnyWordSpec {

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

  object HashMapDeserializer extends RichSerializer[HashMap[String, _]] {

    override def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), HashMap[String, _]] = {
      case (scalaType, JObject(fields)) if classOf[HashMap[_, _]] == scalaType.erasure =>
        scalaType.manifest.typeArguments match {
          case List(_, vType) =>
            HashMap(
              fields.map { case (k, v) =>
                k -> extract(v)(format, vType)
              }: _*
            )
        }
    }

    override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = { case map: HashMap[_, _] =>
      JObject {
        map.map {
          case (k: String, v) => k -> decompose(v)
          case (k, _) => throw new MappingException(s"Expected String key but got $k")
        }.toList
      }
    }
  }

  "it" should {
    "deserialize types which have type params" in {
      implicit val formats: Formats = DefaultFormats + CustomTuple2Serializer + TypeBearerDeserializer
      val json = """[{"name": "foo"}, {"name": "bar"}]"""
      val extracted = JsonMethods.parse(json).extract[(TypeBearer[TypeBearer[String]], TypeBearer[Int])]
      assert(extracted._2.enclosedType == manifest[Int])
      assert(extracted._1.enclosedType == manifest[TypeBearer[String]])
    }

    "serialize with rich serializer logic" in {
      implicit val formats: Formats = DefaultFormats + CustomTuple2Serializer
      assert(Extraction.decompose(("foo", 1)) == JArray(List(JString("foo"), JInt(1))))
    }

    "deserialize hash maps correctly" in {
      implicit val formats: Formats = DefaultFormats + HashMapDeserializer
      val json = """{"map":{"foo": null, "bar": 2}}"""
      val extracted = JsonMethods.parse(json).extract[HashMapHaver]
      assert(extracted == HashMapHaver(HashMap("foo" -> None, "bar" -> Some(2))))
    }

    "be compatible with type hints" in {
      implicit val formats: Formats =
        DefaultFormats + HashMapDeserializer + MappedTypeHints(Map(classOf[HashMapHaver] -> "map_haver"))
      val json = """{"map":{"foo": null, "bar": 2}, "jsonClass": "map_haver"}"""
      val expected = HashMapHaver(HashMap("foo" -> None, "bar" -> Some(2)))
      val extracted = JsonMethods.parse(json).extract[SomeTrait]
      assert(extracted == expected)
    }
  }
}

case class TypeBearer[T: Manifest](name: String) {
  def enclosedType: Manifest[T] = manifest[T]
}

trait SomeTrait

case class HashMapHaver(map: HashMap[String, Option[Int]]) extends SomeTrait

object CustomTuple2Serializer extends RichSerializer[(_, _)] {

  override def deserialize(implicit format: Formats): PartialFunction[(ScalaType, JValue), (_, _)] = {
    case (scalaType, JArray(arr)) if classOf[(_, _)].isAssignableFrom(scalaType.erasure) =>
      require(arr.size == 2)
      (extract(arr.head, scalaType.typeArgs.head), extract(arr(1), scalaType.typeArgs(1)))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = { case (x, y) =>
    JArray(List(decompose(x), decompose(y)))
  }
}
