package org.json4s
package jackson

import collection.JavaConversions._
import java.util.concurrent.ConcurrentHashMap
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.`type`.{ArrayType, TypeFactory}
import collection.mutable

private[jackson] object Types {
  private val cachedTypes: mutable.ConcurrentMap[Manifest[_], JavaType] = new ConcurrentHashMap[Manifest[_], JavaType]()

  def build(factory: TypeFactory, manifest: Manifest[_]): JavaType =
    cachedTypes.getOrElseUpdate(manifest, constructType(factory, manifest))

  private def constructType(factory: TypeFactory, manifest: Manifest[_]): JavaType = {
    if (manifest.erasure.isArray) {
      ArrayType.construct(factory.constructType(manifest.erasure.getComponentType), null, null)
    } else {
      factory.constructParametricType(
        manifest.erasure,
        manifest.typeArguments.map {m => build(factory, m)}.toArray: _*)
    }
  }
}
