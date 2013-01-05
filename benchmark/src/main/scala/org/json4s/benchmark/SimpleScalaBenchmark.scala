package org.json4s.benchmark

import com.google.caliper.SimpleBenchmark

/* from sirthias/scala-benchmarking-template */
trait SimpleScalaBenchmark extends SimpleBenchmark {

  def repeat[@specialized A](reps: Int)(snippet: => A) = {
    val zero = 0.asInstanceOf[A]
    var i = 0
    var result = zero
    while (i < reps) {
      val res = snippet
      if (res != zero) result = res
      i = i + 1
    }
    result
  }
}