package org.json4s


trait NativeImports extends NativeJsonMethods {
  implicit def extractableJValue(jv: JValue) = new JValueExt(jv)
}

object NativeImports extends NativeImports
