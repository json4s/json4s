package org.json4s

import JsonAST._
import scala.language.dynamics

// Should I make this into a subclass of Jvalue?
case class DynamicJValue(jv: JValue) extends Dynamic {
  /**
     * Adds dynamic style to JValues. Only meaningful for JObjects
     * <p>
     * Example:<pre>
     * JObject(JField("name",JString("joe"))::Nil).name == JString("joe")
     * </pre>
     */
  def selectDynamic(name:String):DynamicJValue = jv match {
    case jObj: JObject => {
      jObj.obj.find{ case (n, v) => n == name} match {
        case Some((_, v)) => new DynamicJValue(v)
        case None => new DynamicJValue(JNothing)
      }
    }
    case _ => new DynamicJValue(JNothing)
  }
  
  override def hashCode():Int = jv.hashCode
  
}

object DynamicJValue {
  implicit def dynamicToJv(dynJv: DynamicJValue) = dynJv.jv
  def dyn(jv:JValue) = new DynamicJValue(jv)
}

