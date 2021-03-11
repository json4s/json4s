package org.json4s

import java.io.{StringWriter, Writer => JWriter}
import StreamingJsonWriter._

private final class FieldStreamingJsonWriter[T <: JWriter](
  name: String,
  isFirst: Boolean,
  protected[this] val nodes: T,
  protected[this] val level: Int,
  parent: ObjectStreamingJsonWriter[T],
  protected[this] val pretty: Boolean,
  protected[this] val spaces: Int,
  protected[this] val alwaysEscapeUnicode: Boolean
) extends StreamingJsonWriter[T] {

  def result: T = nodes

  override def startArray(): JsonWriter[T] = {
    writeName(hasPretty = true)
    new ArrayStreamingJsonWriter(nodes, level + 1, parent, pretty, spaces, alwaysEscapeUnicode)
  }

  override def startObject(): JsonWriter[T] = {
    writeName(hasPretty = true)
    new ObjectStreamingJsonWriter(nodes, level + 1, parent, pretty, spaces, alwaysEscapeUnicode)
  }

  // FIXME: hasPretty is no longer used?
  private[this] def writeName(hasPretty: Boolean): Unit = {
    if (!isFirst) {
      nodes.write(",")
      writePretty()
    }
    nodes.append("\"")
    ParserUtil.quote(name, nodes, alwaysEscapeUnicode)
    nodes.append("\":")
  }

  def addNode(node: String): JsonWriter[T] = {
    writeName(hasPretty = false)
    nodes.append(node)
    parent
  }

  def addAndQuoteNode(node: String): JsonWriter[T] = {
    writeName(hasPretty = false)
    nodes.append("\"")
    ParserUtil.quote(node, nodes, alwaysEscapeUnicode = alwaysEscapeUnicode)
    nodes.append("\"")
    parent
  }
}
private final class ObjectStreamingJsonWriter[T <: JWriter](
  protected[this] val nodes: T,
  protected[this] val level: Int,
  parent: StreamingJsonWriter[T],
  protected[this] val pretty: Boolean,
  protected[this] val spaces: Int,
  protected[this] val alwaysEscapeUnicode: Boolean
) extends StreamingJsonWriter[T] {
  nodes write '{'
  writePretty()
  private[this] var isFirst = true
  def result: T = nodes

  def addNode(node: String): JsonWriter[T] = {
    if (isFirst) isFirst = false
    else nodes.write(",")
    nodes write node
    this
  }

  override def endObject(): JsonWriter[T] = {
    writePretty(outdent = 2)
    nodes.write('}')
    parent
  }

  def addAndQuoteNode(node: String): JsonWriter[T] = {
    if (isFirst) isFirst = false
    else nodes.append(",")
    nodes.append("\"")
    ParserUtil.quote(node, nodes, alwaysEscapeUnicode)
    nodes.append("\"")
    this
  }

  override def startArray(): JsonWriter[T] = {
    sys.error(
      "You have to start a field to be able to end it (startArray called before startField in a JObject builder)"
    )
  }

  override def endArray(): JsonWriter[T] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  override def startObject(): JsonWriter[T] =
    sys.error(
      "You have to start a field to be able to end it (startObject called before startField in a JObject builder)"
    )

  override def string(value: String): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (string called before startField in a JObject builder)")

  override def byte(value: Byte): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (byte called before startField in a JObject builder)")

  override def int(value: Int): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (int called before startField in a JObject builder)")

  override def long(value: Long): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (long called before startField in a JObject builder)")

  override def bigInt(value: BigInt): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (bigInt called before startField in a JObject builder)")

  override def boolean(value: Boolean): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (boolean called before startField in a JObject builder)")

  override def short(value: Short): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (short called before startField in a JObject builder)")

  override def float(value: Float): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (float called before startField in a JObject builder)")

  override def double(value: Double): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (double called before startField in a JObject builder)")

  override def bigDecimal(value: BigDecimal): JsonWriter[T] =
    sys.error(
      "You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)"
    )

  override def startField(name: String): JsonWriter[T] = {
    val r = new FieldStreamingJsonWriter(name, isFirst, nodes, level, this, pretty, spaces, alwaysEscapeUnicode)
    if (isFirst) isFirst = false
    r
  }
}
private final class ArrayStreamingJsonWriter[T <: JWriter](
  protected[this] val nodes: T,
  protected[this] val level: Int,
  parent: StreamingJsonWriter[T],
  protected[this] val pretty: Boolean,
  protected[this] val spaces: Int,
  protected[this] val alwaysEscapeUnicode: Boolean
) extends StreamingJsonWriter[T] {
  nodes.write('[')
  writePretty()
  private[this] var isFirst = true
  def result: T = nodes

  override def endArray(): JsonWriter[T] = {
    writePretty(outdent = 2)
    nodes write ']'
    parent
  }

  private[this] def writeComma(): Unit = {
    if (!isFirst) {
      nodes.write(',')
      writePretty()
    } else isFirst = false
  }

  override def startArray(): JsonWriter[T] = {
    writeComma()
    super.startArray()
  }

  override def startObject(): JsonWriter[T] = {
    writeComma()
    super.startObject()
  }

  def addNode(node: String): JsonWriter[T] = {
    writeComma()
    nodes.write(node)
    this
  }

  def addAndQuoteNode(node: String): JsonWriter[T] = {
    writeComma()
    nodes.append("\"")
    ParserUtil.quote(node, nodes, alwaysEscapeUnicode = alwaysEscapeUnicode)
    nodes.append("\"")
    this
  }
}
private final class RootStreamingJsonWriter[T <: JWriter](
  protected[this] val nodes: T = new StringWriter(),
  protected[this] val pretty: Boolean = false,
  protected[this] val spaces: Int = 2,
  protected[this] val alwaysEscapeUnicode: Boolean = false
) extends StreamingJsonWriter[T] {

  protected[this] val level: Int = 0

  def addNode(node: String): JsonWriter[T] = {
    nodes write node
    this
  }

  def addAndQuoteNode(node: String): JsonWriter[T] = {
    nodes.append("\"")
    ParserUtil.quote(node, nodes, alwaysEscapeUnicode = alwaysEscapeUnicode)
    nodes.append("\"")
    this
  }

  def result: T = nodes
}

private sealed abstract class StreamingJsonWriter[T <: JWriter] extends JsonWriter[T] {

  protected[this] def level: Int
  protected[this] def spaces: Int
  protected[this] def pretty: Boolean
  protected[this] def nodes: T
  protected[this] val alwaysEscapeUnicode: Boolean

  def startArray(): JsonWriter[T] = {
    new ArrayStreamingJsonWriter(nodes, level + 1, this, pretty, spaces, alwaysEscapeUnicode)
  }

  def startObject(): JsonWriter[T] = {
    new ObjectStreamingJsonWriter(nodes, level + 1, this, pretty, spaces, alwaysEscapeUnicode)
  }

  def addNode(node: String): JsonWriter[T]
  def addAndQuoteNode(node: String): JsonWriter[T]

  def endObject(): JsonWriter[T] = {
    sys.error("You have to start an object to be able to end it (endObject called before startObject)")
  }

  def startField(name: String): JsonWriter[T] = {
    sys.error("You have to start an object before starting a field.")
  }

  def string(value: String): JsonWriter[T] = addAndQuoteNode(value)

  def byte(value: Byte): JsonWriter[T] = addNode(value.toString)

  def int(value: Int): JsonWriter[T] = addNode(value.toString)

  def long(value: Long): JsonWriter[T] = addNode(value.toString)

  def bigInt(value: BigInt): JsonWriter[T] = addNode(value.toString())

  def boolean(value: Boolean): JsonWriter[T] = addNode(if (value) "true" else "false")

  def short(value: Short): JsonWriter[T] = addNode(value.toString)

  def endArray(): JsonWriter[T] = {
    sys.error("You have to start an object to be able to end it (endArray called before startArray)")
  }

  def float(value: Float): JsonWriter[T] = addNode(handleInfinity(value))

  def double(value: Double): JsonWriter[T] = addNode(handleInfinity(value))

  def bigDecimal(value: BigDecimal): JsonWriter[T] = addNode(value.toString())

  def resultString: String = result.toString

  def addJValue(jv: JValue): JsonWriter[T] = jv match {
    case JNull => addNode("null")
    case JString(str) => string(str)
    case JInt(i) => bigInt(i)
    case JLong(i) => long(i)
    case JDouble(d) => double(d)
    case JDecimal(d) => bigDecimal(d)
    case JBool(b) => boolean(b)

    case JArray(arr) =>
      val ab = startArray()
      arr foreach ab.addJValue
      ab.endArray()

    case JSet(s) =>
      val ab = startArray()
      s foreach ab.addJValue
      ab.endArray()

    case JObject(flds) =>
      val obj = startObject()
      flds foreach {
        case (_, v) if v == JNothing => this
        case (k, v) => obj.startField(k).addJValue(v)
      }
      obj.endObject()

    case JNothing => this
  }

  protected def writePretty(outdent: Int = 0): Unit = {
    if (pretty) {
      nodes write '\n'
      nodes.write(" " * (level * spaces - outdent))
    }
  }
}

private[json4s] object StreamingJsonWriter {

  private[this] val posInfinityVal = "1e+500"
  private[this] val negInfiniteVal = "-1e+500"

  private[json4s] def handleInfinity(value: Float): String = {
    if (value.isPosInfinity) {
      posInfinityVal
    } else if (value.isNegInfinity) {
      negInfiniteVal
    } else {
      value.toString
    }
  }
  private[json4s] def handleInfinity(value: Double): String = {
    if (value.isPosInfinity) {
      posInfinityVal
    } else if (value.isNegInfinity) {
      negInfiniteVal
    } else {
      value.toString
    }
  }
}
