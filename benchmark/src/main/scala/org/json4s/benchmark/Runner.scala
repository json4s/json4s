package org.json4s.benchmark

import com.google.caliper.{ Runner => CaliperRunner}

object Runner {
  def main(args: Array[String]) {
    CaliperRunner.main(classOf[SerializationNoTypeHintsBenchmark], args)
  }
}