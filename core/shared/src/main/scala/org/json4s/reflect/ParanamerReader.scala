package org.json4s.reflect

import scala.collection.immutable.ArraySeq

object ParanamerReader extends ParameterNameReader {
  def lookupParameterNames(constructor: Executable): Seq[String] =
    constructor.getAsAccessibleObject match {
      case executable: java.lang.reflect.Executable =>
        ArraySeq.unsafeWrapArray(
          executable.getParameters.map(_.getName)
        )
      case _ =>
        Nil
    }
}
