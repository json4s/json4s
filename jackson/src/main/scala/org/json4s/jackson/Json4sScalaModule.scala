package org.json4s
package jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.Module.SetupContext

object Json4sModule {
  private val VersionRegex = """(\d+)\.(\d+)(?:\.(\d+)(?:\-(.*))?)?""".r
  val version: Version = try {
    val groupId = BuildInfo.organization
    val artifactId = BuildInfo.name
    BuildInfo.version match {
      case VersionRegex(major,minor,patchOpt,snapOpt) => {
        val patch = Option(patchOpt) map (_.toInt) getOrElse 0
        new Version(major.toInt,minor.toInt,patch,snapOpt,groupId,artifactId)
      }
      case _ => Version.unknownVersion()
    }
  } catch { case _: Throwable => Version.unknownVersion() }
}

class Json4sScalaModule extends Module {

  def getModuleName: String = "Json4sScalaModule"

  def version(): Version = Json4sModule.version

  def setupModule(ctxt: SetupContext): Unit = {
    ctxt.addSerializers(JValueSerializerResolver)
    ctxt.addDeserializers(JValueDeserializerResolver)
  }
}

object Json4sScalaModule extends Json4sScalaModule
