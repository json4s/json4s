package com.tt.json4s.ext

import java.util.TimeZone

import com.tt.json4s.{Formats, MappingException}

object DateParser {

  def parse(s: String, format: Formats): ZonedInstant = {
    val instant =
      format.dateFormat.parse(s).map(_.getTime).getOrElse(throw new MappingException(s"Invalid date format $s"))
    val timezone = format.dateFormat.timezone
    ZonedInstant(instant, timezone)
  }

  case class ZonedInstant(instant: Long, timezone: TimeZone)

}
