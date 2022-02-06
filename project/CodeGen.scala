object CodeGen {
  private[this] def max = 22

  def format: String = {
    s"""package org.json4s

trait FormatFunctions { self: JsonFormat.type =>
${(2 to max).map(formatN).mkString("\n")}
}
"""
  }

  private[this] def formatN(n: Int): String = {
    val A = (1 to n).map("A" + _)
    def signature(name: String): String = s"""def ${name}[${A.mkString(", ")}, X](
    apply: (${A.mkString(", ")}) => X, unapply: X => (${A.mkString(", ")})
  )(${(1 to n).map("key" + _ + ": String").mkString(", ")})(implicit
    ${A.map(a => s"${a}: JsonFormat[${a}]").mkString(", ")}
  ): JsonFormat[X]"""

    s"""  ${signature("format")} =
    format${n}[${A.mkString(", ")}, X](apply, unapply)(${(1 to n).map(n => s"key${n}").mkString(", ")})

  ${signature("format" + n)} = JsonFormat.GenericFormat(
    reader = Reader.reader${n}(apply)(${(1 to n).map("key" + _).mkString(", ")}),
    writer = Writer.writer${n}(unapply)(${(1 to n).map("key" + _).mkString(", ")})
  )
"""
  }

  def writer(addAuto: Boolean): String = {
    s"""package org.json4s

trait WriterFunctions { self: Writer.type =>
${(2 to max).map(writerN).mkString("\n")}

${if (addAuto) (2 to max).map(writerAutoN).mkString("\n") else ""}
}
"""
  }

  private[this] def writerAutoN(n: Int): String = {
    val A = (1 to n).map("A" + _)
    def signature(name: String): String = s"""def ${name}[${A.mkString(", ")}, X <: Product](f: X => (${A
        .mkString(", ")}))(implicit
    ${A.map(a => s"${a}: Writer[${a}]").mkString(", ")}
  ): Writer[X]"""

    s"""  ${signature("writerAuto")} =
    writerAuto${n}[${A.mkString(", ")}, X](f)

  ${signature("writerAuto" + n)} = new Writer[X] {
    def write(obj: X): JValue = {
      val (${(1 to n).map("a" + _).mkString(", ")}) = f(obj)
      JObject(
        ${(1 to n).map(i => s"(obj.productElementName(${i - 1}), A${i}.write(a${i}))").mkString("", " :: ", " :: Nil")}
      )
    }
  }
"""
  }

  private[this] def writerN(n: Int): String = {
    val A = (1 to n).map("A" + _)
    def signature(name: String): String = s"""def ${name}[${A.mkString(", ")}, X](f: X => (${A
        .mkString(", ")}))(${(1 to n).map("key" + _ + ": String").mkString(", ")})(implicit
    ${A.map(a => s"${a}: Writer[${a}]").mkString(", ")}
  ): Writer[X]"""

    s"""  ${signature("writer")} =
    writer${n}[${A.mkString(", ")}, X](f)(${(1 to n).map(n => s"key${n}").mkString(", ")})

  ${signature("writer" + n)} = new Writer[X] {
    def write(obj: X): JValue = {
      val (${(1 to n).map("a" + _).mkString(", ")}) = f(obj)
      JObject(
        ${(1 to n).map(i => s"(key${i}, A${i}.write(a${i}))").mkString("", " :: ", " :: Nil")}
      )
    }
  }
"""
  }

  def reader: String = {
    s"""package org.json4s

trait ReaderFunctions { self: Reader.type =>
${(2 to max).map(readerN).mkString("\n")}
}
"""
  }

  private[this] def readerN(n: Int): String = {
    val A = (1 to n).map("A" + _)
    val fields = (1 to n)
      .map(x =>
        s"""      val a${x} = obj.get(key${x}).toRight(new MappingException("field " + key${x} + " not found")).flatMap(A${x} readEither _)"""
      )
      .mkString("\n")
    val lefts = (1 to n)
      .map(x => s"""      a${x} match {
        case Left(l) =>
          lefts ::= l
        case _ =>
      }""")
      .mkString("\n")

    def signature(name: String): String = s"""
  def ${name}[${A.mkString(", ")}, X](f: (${A.mkString(", ")}) => X)(
    ${(1 to n).map(n => s"key${n}: String").mkString(", ")}
  )(implicit
    ${A.map(x => s"${x}: Reader[${x}]").mkString(", ")}
  ): Reader[X]"""

    s"""${signature("reader")} =
    reader${n}[${A.mkString(", ")}, X](f)(${(1 to n).map(n => s"key${n}").mkString(", ")})

${signature("reader" + n)} = Reader.from {
    case json: JObject =>
      val obj = json.obj.toMap
${fields}
      var lefts = List.empty[MappingException]
${lefts}
      if (lefts.isEmpty) {
        Right(f(${(1 to n).map(x => s"a${x}.asInstanceOf[Right[_, A${x}]].value").mkString(", ")}))
      } else {
        Left(new MappingException.Multi(lefts.reverse, null))
      }
    case x =>
      Left(new MappingException("JObject expected, but got " + x))
  }
"""
  }
}
