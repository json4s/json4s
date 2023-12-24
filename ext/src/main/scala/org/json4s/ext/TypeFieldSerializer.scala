package org.json4s.ext

import org.json4s.{
  CustomSerializer,
  DefaultFormats,
  Extraction,
  Formats,
  JField,
  JObject,
  JString,
  JValue,
  NoTypeHints,
  TypeHints,
  jvalue2monadic
}
import org.json4s.reflect.{Reflector, ScalaType}

class TypeFieldSerializer[T: Manifest](fieldName: String, mapping: Map[String, Class[? <: T]])
  extends CustomSerializer[T](fm => {
    implicit val format: Formats = new Formats {
      val dateFormat = DefaultFormats.lossless.dateFormat
      override val typeHints: TypeHints = NoTypeHints
    }
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
