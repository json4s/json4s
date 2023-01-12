package org.json4s.reflect

object ParanamerReader extends ParameterNameReader {
  def lookupParameterNames(constructor: Executable): Seq[String] =
    constructor.getAsAccessibleObject match {
      case executable: java.lang.reflect.Executable =>
        executable.getParameters.map(_.getName)
      case _ =>
        Nil
    }
}
