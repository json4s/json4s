package org.json4s
package jackson

trait JacksonImports extends JacksonJsonMethods {
  implicit def extendedJValue(jv: JValue) = new JValueExt(jv)
}

object JacksonImports extends JacksonImports
