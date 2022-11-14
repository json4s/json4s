package com.tt.json4s

import scala.language.dynamics

class DynamicJValue(val raw: JValue) extends Dynamic {

  /**
   * Adds dynamic style to JValues. Only meaningful for JObjects
   * <p>
   * Example:<pre>
   * JObject(JField("name",JString("joe"))::Nil).name == JString("joe")
   * </pre>
   */
  def selectDynamic(name: String): DynamicJValue = new DynamicJValue(new MonadicJValue(raw) \ name)

  override def hashCode(): Int = raw.hashCode

  override def equals(p1: Any): Boolean = p1 match {
    case j: DynamicJValue => raw == j.raw
    case j: JValue => raw == j
    case _ => false
  }
}

trait DynamicJValueImplicits {
  implicit def dynamic2Jv(dynJv: DynamicJValue): JValue = dynJv.raw
  implicit def dynamic2monadic(dynJv: DynamicJValue): MonadicJValue = new MonadicJValue(dynJv.raw)
  def dyn(jv: JValue) = new DynamicJValue(jv)
}

object DynamicJValue extends DynamicJValueImplicits
