package org.json4s

package object native {

  implicit def jvalue2jvalueWithExtraction(jv: JValue) = new JValueExt(jv)
}