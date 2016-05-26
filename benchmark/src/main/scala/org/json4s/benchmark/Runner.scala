package org.json4s.benchmark

import com.google.caliper.{ Runner => CaliperRunner}

object Runner {
  def main(args: Array[String]): Unit = {
    CaliperRunner.main(classOf[Json4sBenchmark], args)
  }
}
