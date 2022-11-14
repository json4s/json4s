package com.tt.json4s.reflect

trait ParameterNameReader {
  def lookupParameterNames(constructor: Executable): Seq[String]
}
