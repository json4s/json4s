package org.json4s

class MappingException(val msg: String, val cause: Exception) extends Exception(msg, cause) {
  def this(msg: String) = this(msg, null)
}

object MappingException {
  class Multi(val errors: Seq[MappingException], cause: Exception)
    extends MappingException(
      msg = errors.map(_.msg).mkString(", "),
      cause = cause
    ) {
    def this(errors: Seq[MappingException]) = this(errors, errors.headOption.orNull)
  }
}
