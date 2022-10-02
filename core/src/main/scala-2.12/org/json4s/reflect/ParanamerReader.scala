package org.json4s.reflect

import com.thoughtworks.paranamer.{BytecodeReadingParanamer, CachingParanamer}

object ParanamerReader extends ParameterNameReader {
  private[this] val paranamer = new CachingParanamer(new BytecodeReadingParanamer)
  def lookupParameterNames(constructor: Executable): Seq[String] =
    paranamer.lookupParameterNames(constructor.getAsAccessibleObject).toSeq
}
