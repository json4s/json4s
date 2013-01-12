package org.json4s
package examples

trait Benchmark {
  def run(name: String, warmup: Int, count: Int)(f: => Any) = {
    repeat(warmup)(f)
    System.gc
    val t = time {
      repeat(count)(f)
    }
    val extra = if (t > 10000) 0
    else if (t > 1000) 1
    else if (t > 100) 2
    else if (t > 10) 3
    else 4
    val padding = 32 - name.size + extra
    println(name + (" " * padding) + t + "ms  ")
  }

  def repeat(count: Int)(f: => Any) = {
    var i = 0; while (i < count) {
      f
      i += 1
    }
  }

  def time(f: => Any): Long = {
    val start = System.currentTimeMillis
    f
    System.currentTimeMillis - start
  }
}



