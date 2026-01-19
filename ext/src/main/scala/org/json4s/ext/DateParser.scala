package org.json4s.ext

import java.util.TimeZone
import org.json4s.Formats
import org.json4s.MappingException

object DateParser {

  def parse(s: String, format: Formats): ZonedInstant = {
    val instant =
      format.dateFormat.parse(s).map(_.getTime).getOrElse(throw new MappingException(s"Invalid date format $s"))
    val timezone = format.dateFormat.timezone
    ZonedInstant(instant, timezone)
  }

  case class ZonedInstant(instant: Long, timezone: TimeZone)

}
