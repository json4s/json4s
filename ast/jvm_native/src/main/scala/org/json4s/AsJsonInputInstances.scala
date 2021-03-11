package org.json4s

trait AsJsonInputInstances { self: AsJsonInput.type =>

  implicit final val fileAsJsonInput: AsJsonInput[java.io.File] =
    fromFunction(x => FileInput(x))

}
