package org.json4s
package jackson

import tools.jackson.core.Version
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.JacksonModule.SetupContext
import tools.jackson.databind.module.SimpleDeserializers

object Json4sModule {
  private[this] val VersionRegex = """(\d+)\.(\d+)(?:\.(\d+)(?:\-(.*))?)?""".r
  val version: Version =
    try {
      val groupId = BuildInfo.organization
      val artifactId = BuildInfo.name
      BuildInfo.version match {
        case VersionRegex(major, minor, patchOpt, snapOpt) => {
          val patch = Option(patchOpt) map (_.toInt) getOrElse 0
          new Version(major.toInt, minor.toInt, patch, snapOpt, groupId, artifactId)
        }
        case _ => Version.unknownVersion()
      }
    } catch { case _: Throwable => Version.unknownVersion() }
}

class Json4sScalaModule extends JacksonModule {

  def getModuleName: String = "Json4sScalaModule"

  def version(): Version = Json4sModule.version

  private val classes: Seq[Class[?]] = Seq[Class[?]](
    classOf[JValue],
    classOf[JArray],
    classOf[JBool],
    classOf[JDecimal],
    classOf[JDouble],
    classOf[JInt],
    classOf[JLong],
    classOf[JNothing.type],
    classOf[JNull.type],
    classOf[JNumber],
    classOf[JObject],
    classOf[JSet],
    classOf[JString],
  )

  def setupModule(ctxt: SetupContext): Unit = {
    ctxt.addSerializers(JValueSerializerResolver)
    val deserializer = new SimpleDeserializers()
    val jValueDeserializer = new JValueDeserializer(classOf[JValue])
    classes.foreach(c => deserializer.addDeserializer(c.asInstanceOf[Class[Object]], jValueDeserializer))
    ctxt.addDeserializers(deserializer)
  }
}

object Json4sScalaModule extends Json4sScalaModule
