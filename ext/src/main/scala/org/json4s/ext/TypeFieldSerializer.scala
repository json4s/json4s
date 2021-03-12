package org.json4s.ext

import org.json4s.JsonAST.{JField, JObject, JString}
import org.json4s.reflect.{Reflector, ScalaType}
import org.json4s.{CustomSerializer, Extraction, JValue, NoTypeHints, native}

class TypeFieldSerializer[T: Manifest](fieldName: String, mapping: Map[String, Class[_ <: T]])
  extends CustomSerializer[T](fm => {
    implicit val format = native.Serialization.formats(NoTypeHints)
    val indexByType: Map[ScalaType, String] = mapping.map { case (k, v) => Reflector.scalaTypeOf(v) -> k }
    val indexByName: Map[String, ScalaType] = mapping.mapValues(Reflector.scalaTypeOf).toMap
    val deserialize: PartialFunction[JValue, T] = { case ast =>
      val JString(fieldValue) = ast \ fieldName
      val scalaType = indexByName(fieldValue)
      Extraction.extract(ast, scalaType).asInstanceOf[T]
    }
    val serialize: PartialFunction[Any, JValue] = { case obj: T =>
      val scalaType = Reflector.scalaTypeOf(obj.getClass)
      val JObject(fields) = Extraction.decompose(obj)
      val typeField = JField(fieldName, JString(indexByType(scalaType)))
      JObject(typeField :: fields)
    }
    (deserialize, serialize)
  })
