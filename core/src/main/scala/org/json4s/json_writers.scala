package org.json4s

import java.io.{ StringWriter, Writer => JWriter }
import scala.collection.mutable.ListBuffer
import StreamingJsonWriter._

object JsonWriter {
  def ast: JsonWriter[JValue] = new JDoubleAstRootJsonWriter
  def bigDecimalAst: JsonWriter[JValue] = new JDecimalAstRootJsonWriter
  def streaming[T <: java.io.Writer](writer: T)(implicit formats: Formats = DefaultFormats): JsonWriter[T] = new RootStreamingJsonWriter[T](writer, pretty = false, formats = formats)
  def streamingPretty[T <: java.io.Writer](writer: T)(implicit formats: Formats = DefaultFormats): JsonWriter[T] = new RootStreamingJsonWriter[T](writer, pretty = true, formats = formats)
}
trait JsonWriter[T] {
  def startArray(): JsonWriter[T]
  def endArray(): JsonWriter[T]
  def startObject(): JsonWriter[T]
  def endObject(): JsonWriter[T]
  def string(value: String): JsonWriter[T]
  def byte(value: Byte): JsonWriter[T]
  def int(value: Int): JsonWriter[T]
  def long(value: Long): JsonWriter[T]
  def bigInt(value: BigInt): JsonWriter[T]
  def boolean(value: Boolean): JsonWriter[T]
  def short(value: Short): JsonWriter[T]
  def float(value: Float): JsonWriter[T]
  def double(value: Double): JsonWriter[T]
  def bigDecimal(value: BigDecimal): JsonWriter[T]
  def startField(name: String): JsonWriter[T]
  def result: T

  def addJValue(jv: JValue): JsonWriter[T]
}
private final class JDoubleJFieldJsonWriter(name: String, parent: JDoubleJObjectJsonWriter) extends JDoubleAstJsonWriter {
  def result: JValue = JNothing


  def addNode(node: JValue): JsonWriter[JValue] = parent.addNode(name -> node)


}
private final class JDoubleAstRootJsonWriter extends JDoubleAstJsonWriter {
  private[this] var nodes = List.empty[JValue]

  def addNode(node: JValue): JsonWriter[JValue] = {
    nodes ::= node
    this
  }
  def result: JValue = {
    if (nodes.nonEmpty) nodes.head else JNothing
  }
}
private final class JDecimalJFieldJsonWriter(name: String, parent: JDecimalJObjectJsonWriter) extends JDecimalAstJsonWriter {
  def result: JValue = JNothing


  def addNode(node: JValue): JsonWriter[JValue] = parent.addNode(name -> node)


}
private final class JDecimalAstRootJsonWriter extends JDecimalAstJsonWriter {
  private[this] var nodes = List.empty[JValue]

  def addNode(node: JValue): JsonWriter[JValue] = {
    nodes ::= node
    this
  }
  def result: JValue = {
    if (nodes.nonEmpty) nodes.head else JNothing
  }
}
private final class JDoubleJObjectJsonWriter(parent: JsonWriter[JValue]) extends JsonWriter[JValue] {
  private[this] val nodes = ListBuffer[JField]()
  def addNode(node: JField): JDoubleJObjectJsonWriter = {
    nodes append node
    this
  }
  def startArray(): JsonWriter[JValue] = {
    sys.error("You have to start a field to be able to end it (startArray called before startField in a JObject builder)")
  }

  def endArray(): JsonWriter[JValue] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  def startObject(): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (startObject called before startField in a JObject builder)")

  def endObject(): JsonWriter[JValue] = {
    parent match {
      case p: JDoubleAstJsonWriter => p.addNode(result)
      case _ => parent
    }
  }

  def string(value: String): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (string called before startField in a JObject builder)")

  def byte(value: Byte): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (byte called before startField in a JObject builder)")

  def int(value: Int): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (int called before startField in a JObject builder)")

  def long(value: Long): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (long called before startField in a JObject builder)")

  def bigInt(value: BigInt): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (bigInt called before startField in a JObject builder)")

  def boolean(value: Boolean): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (boolean called before startField in a JObject builder)")

  def short(value: Short): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (short called before startField in a JObject builder)")

  def float(value: Float): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (float called before startField in a JObject builder)")

  def double(value: Double): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (double called before startField in a JObject builder)")

  def bigDecimal(value: BigDecimal): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)")

  def startField(name: String): JsonWriter[JValue] = new JDoubleJFieldJsonWriter(name, this)


  def addJValue(jv: _root_.org.json4s.JValue): JsonWriter[_root_.org.json4s.JValue] =
    sys.error("You have to start a field to be able to end it (addJValue called before startField in a JObject builder)")

  def result: JValue = JObject(nodes.toList)
}
private final class JDecimalJObjectJsonWriter(parent: JsonWriter[JValue]) extends JsonWriter[JValue] {
  private[this] val nodes = ListBuffer[JField]()
  def addNode(node: JField): JDecimalJObjectJsonWriter = {
    nodes append node
    this
  }
  def startArray(): JsonWriter[JValue] = {
    sys.error("You have to start a field to be able to end it (startArray called before startField in a JObject builder)")
  }

  def endArray(): JsonWriter[JValue] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  def startObject(): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (startObject called before startField in a JObject builder)")

  def endObject(): JsonWriter[JValue] = {
    parent match {
      case p: JDecimalAstJsonWriter => p.addNode(result)
      case _ => parent
    }
  }

  def string(value: String): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (string called before startField in a JObject builder)")

  def byte(value: Byte): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (byte called before startField in a JObject builder)")

  def int(value: Int): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (int called before startField in a JObject builder)")

  def long(value: Long): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (long called before startField in a JObject builder)")

  def bigInt(value: BigInt): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (bigInt called before startField in a JObject builder)")

  def boolean(value: Boolean): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (boolean called before startField in a JObject builder)")

  def short(value: Short): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (short called before startField in a JObject builder)")

  def float(value: Float): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (float called before startField in a JObject builder)")

  def double(value: Double): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (double called before startField in a JObject builder)")

  def bigDecimal(value: BigDecimal): JsonWriter[JValue] =
    sys.error("You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)")

  def startField(name: String): JsonWriter[JValue] = new JDecimalJFieldJsonWriter(name, this)


  def addJValue(jv: _root_.org.json4s.JValue): JsonWriter[_root_.org.json4s.JValue] =
    sys.error("You have to start a field to be able to end it (addJValue called before startField in a JObject builder)")

  def result: JValue = JObject(nodes.toList:_*)
}

private final class JDoubleJArrayJsonWriter(parent: JsonWriter[JValue]) extends JDoubleAstJsonWriter {
  private[this] val nodes = ListBuffer[JValue]()
  def addNode(node: JValue): JsonWriter[JValue] = {
    nodes append node
    this
  }

  override def endArray(): JsonWriter[JValue] = {
    parent match {
      case m: JDoubleAstJsonWriter => m.addNode(result)
      case _ => parent
    }
  }

  def result: JValue = JArray(nodes.toList)
}

private final class JDecimalJArrayJsonWriter(parent: JsonWriter[JValue]) extends JDecimalAstJsonWriter {
  private[this] val nodes = ListBuffer[JValue]()
  def addNode(node: JValue): JsonWriter[JValue] = {
    nodes append node
    this
  }

  override def endArray(): JsonWriter[JValue] = {
    parent match {
      case m: JDecimalAstJsonWriter => m.addNode(result)
      case _ => parent
    }
  }

  def result: JValue = JArray(nodes.toList)
}
private sealed abstract class JValueJsonWriter extends JsonWriter[JValue] {

  def addNode(node: JValue): JsonWriter[JValue]

  def endObject(): JsonWriter[JValue] = {
    sys.error("You have to start an object to be able to end it (endObject called before startObject)")
  }


  def startField(name: String): JsonWriter[JValue] = {
    sys.error("You have to start an object before starting a field.")
  }

  def string(value: String): JsonWriter[JValue] = addNode(JString(value))

  def byte(value: Byte): JsonWriter[JValue] = addNode(JInt(value))

  def int(value: Int): JsonWriter[JValue] = addNode(JInt(value))

  def long(value: Long): JsonWriter[JValue] = addNode(JInt(value))

  def bigInt(value: BigInt): JsonWriter[JValue] = addNode(JInt(value))

  def boolean(value: Boolean): JsonWriter[JValue] = addNode(JBool(value))

  def short(value: Short): JsonWriter[JValue] = addNode(JInt(value))

  def endArray(): JsonWriter[JValue] = {
    sys.error("You have to start an object to be able to end it (endArray called before startArray)")
  }

  def addJValue(jv: JValue): JsonWriter[JValue] = addNode(jv)

}
private sealed abstract class JDoubleAstJsonWriter extends JValueJsonWriter {
  def startArray(): JsonWriter[JValue] = {
    new JDoubleJArrayJsonWriter(this)
  }

  def startObject(): JsonWriter[JValue] = {
    new JDoubleJObjectJsonWriter(this)
  }


  def float(value: Float): JsonWriter[JValue] = addNode(JDouble(value))

  def double(value: Double): JsonWriter[JValue] = addNode(JDouble(value))

  def bigDecimal(value: BigDecimal): JsonWriter[JValue] = addNode(JDouble(value.doubleValue()))
}

private sealed abstract class JDecimalAstJsonWriter extends JValueJsonWriter {
  def startArray(): JsonWriter[JValue] = {
    new JDecimalJArrayJsonWriter(this)
  }

  def startObject(): JsonWriter[JValue] = {
    new JDecimalJObjectJsonWriter(this)
  }

  def float(value: Float): JsonWriter[JValue] = double(value.toDouble)

  def double(value: Double): JsonWriter[JValue] = addNode(JDecimal(BigDecimal(value)))

  def bigDecimal(value: BigDecimal): JsonWriter[JValue] = addNode(JDecimal(value))

}

private final class FieldStreamingJsonWriter[T <: JWriter](name: String, isFirst: Boolean, protected[this] val nodes: T, protected[this] val level: Int, parent: ObjectStreamingJsonWriter[T], protected[this] val pretty: Boolean, protected[this] val spaces: Int, protected[this] val formats: Formats) extends StreamingJsonWriter[T] {
  def result: T = nodes


  override def startArray(): JsonWriter[T] = {
    writeName(hasPretty = true)
    new ArrayStreamingJsonWriter(nodes, level + 1, parent, pretty, spaces, formats)
  }

  override def startObject(): JsonWriter[T] = {
    writeName(hasPretty = true)
    new ObjectStreamingJsonWriter(nodes, level + 1, parent, pretty, spaces, formats)
  }

  private[this] def writeName(hasPretty: Boolean): Unit = {
    if (!isFirst) {
      nodes.write(",")
      writePretty()
    }
    nodes.append("\"")
    ParserUtil.quote(name, nodes)(formats)
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
    ParserUtil.quote(node, nodes)(formats)
    nodes.append("\"")
    parent
  }
}
private final class ObjectStreamingJsonWriter[T <: JWriter](protected[this] val nodes: T, protected[this] val level: Int, parent: StreamingJsonWriter[T], protected[this] val pretty: Boolean, protected[this] val spaces: Int, protected[this] val formats: Formats) extends StreamingJsonWriter[T] {
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
    ParserUtil.quote(node, nodes)(formats)
    nodes.append("\"")
    this
  }

  override def startArray(): JsonWriter[T] = {
    sys.error("You have to start a field to be able to end it (startArray called before startField in a JObject builder)")
  }

  override def endArray(): JsonWriter[T] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  override def startObject(): JsonWriter[T] =
    sys.error("You have to start a field to be able to end it (startObject called before startField in a JObject builder)")


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
    sys.error("You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)")

  override def startField(name: String): JsonWriter[T] = {
    val r = new FieldStreamingJsonWriter(name, isFirst, nodes, level, this, pretty, spaces, formats)
    if (isFirst) isFirst = false
    r
  }
}
private final class ArrayStreamingJsonWriter[T <: JWriter](protected[this] val nodes: T, protected[this] val level: Int, parent: StreamingJsonWriter[T], protected[this] val pretty: Boolean, protected[this] val spaces: Int, protected[this] val formats: Formats) extends StreamingJsonWriter[T] {
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
    }
    else isFirst = false
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
    ParserUtil.quote(node, nodes)(formats)
    nodes.append("\"")
    this
  }
}
private final class RootStreamingJsonWriter[T <: JWriter](protected[this] val nodes: T = new StringWriter(), protected[this] val pretty: Boolean = false, protected[this] val spaces: Int = 2, protected[this] val formats: Formats = DefaultFormats) extends StreamingJsonWriter[T] {

  protected[this] val level: Int = 0

  def addNode(node: String): JsonWriter[T] = {
    nodes write node
    this
  }


  def addAndQuoteNode(node: String): JsonWriter[T] = {
    nodes.append("\"")
    ParserUtil.quote(node, nodes)(formats)
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
  protected[this] val formats: Formats

  def startArray(): JsonWriter[T] = {
    new ArrayStreamingJsonWriter(nodes, level + 1, this, pretty, spaces, formats)
  }

  def startObject(): JsonWriter[T] = {
    new ObjectStreamingJsonWriter(nodes, level + 1, this, pretty, spaces, formats)
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
    case JDouble(d) => double(d)
    case JDecimal(d) => bigDecimal(d)
    case JBool(b) => boolean(b)

    case JArray(arr) =>
      val ab = startArray()
      arr foreach ab.addJValue
      ab.endArray()

    case JObject(flds) =>
      val obj = startObject()
      flds foreach {
        case (k, v) if v == JNothing => this
        case (k, v) => obj.startField(k).addJValue(v)
      }
      obj.endObject()

    case _ => this
  }

  protected def writePretty(outdent: Int = 0): Unit = {
    if (pretty) {
      nodes write '\n'
      nodes.write((" " * (level * spaces - outdent)))
    }
  }
}

private object StreamingJsonWriter {

  private val posInfinityVal = "1e+500"
  private val negInfiniteVal = "-1e+500"

  private def handleInfinity(value: Float): String = {
    if(value.isPosInfinity) {
      posInfinityVal
    } else if (value.isNegInfinity){
      negInfiniteVal
    } else {
      value.toString
    }
  }
  private def handleInfinity(value: Double): String = {
    if(value.isPosInfinity) {
      posInfinityVal
    } else if (value.isNegInfinity){
      negInfiniteVal
    } else {
      value.toString
    }
  }
}
