package org.json4s
package native

/**
 * Serializer which serializes all fields of a class too.
 *
 * Serialization can be intercepted by giving two optional PartialFunctions as
 * constructor parameters:
 * <p>
 * <pre>
 * FieldSerializer[WildDog](
 *   renameTo("name", "animalname") orElse ignore("owner"),
 *   renameFrom("animalname", "name")
 * )
 * </pre>
 */
case class FieldSerializer[A: Manifest](
  serializer:   PartialFunction[(String, Any), Option[(String, Any)]] = Map(),
  deserializer: PartialFunction[JField, JField] = Map()
)

object FieldSerializer {
  def renameFrom(name: String, newName: String): PartialFunction[JField, JField] = {
    case JField(`name`, x) => JField(newName, x)
  }

  def ignore(name: String): PartialFunction[(String, Any), Option[(String, Any)]] = {
    case (`name`, _) => None
  }

  def renameTo(name: String, newName: String): PartialFunction[(String, Any), Option[(String, Any)]] = {
    case (`name`, x) => Some(newName, x)
  }
}
