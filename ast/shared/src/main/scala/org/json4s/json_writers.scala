package org.json4s

import scala.collection.mutable.ListBuffer
import org.json4s.JsonAST._

object JsonWriter {
  def ast: JsonWriter[JValue] = new JDoubleAstRootJsonWriter
  def bigDecimalAst: JsonWriter[JValue] = new JDecimalAstRootJsonWriter
  def streaming[T <: java.io.Writer](writer: T, alwaysEscapeUnicode: Boolean): JsonWriter[T] =
    new RootStreamingJsonWriter[T](writer, pretty = false, alwaysEscapeUnicode = alwaysEscapeUnicode)
  def streamingPretty[T <: java.io.Writer](writer: T, alwaysEscapeUnicode: Boolean): JsonWriter[T] =
    new RootStreamingJsonWriter[T](writer, pretty = true, alwaysEscapeUnicode = alwaysEscapeUnicode)
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
private final class JDoubleJFieldJsonWriter(name: String, parent: JDoubleJObjectJsonWriter)
  extends JDoubleAstJsonWriter {
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
private final class JDecimalJFieldJsonWriter(name: String, parent: JDecimalJObjectJsonWriter)
  extends JDecimalAstJsonWriter {
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
    nodes += node
    this
  }
  def startArray(): JsonWriter[JValue] = {
    sys.error(
      "You have to start a field to be able to end it (startArray called before startField in a JObject builder)"
    )
  }

  def endArray(): JsonWriter[JValue] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  def startObject(): JsonWriter[JValue] =
    sys.error(
      "You have to start a field to be able to end it (startObject called before startField in a JObject builder)"
    )

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
    sys.error(
      "You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)"
    )

  def startField(name: String): JsonWriter[JValue] = new JDoubleJFieldJsonWriter(name, this)

  def addJValue(jv: JValue): JsonWriter[JValue] =
    sys.error(
      "You have to start a field to be able to end it (addJValue called before startField in a JObject builder)"
    )

  def result: JValue = JObject(nodes.toList)
}
private final class JDecimalJObjectJsonWriter(parent: JsonWriter[JValue]) extends JsonWriter[JValue] {
  private[this] val nodes = ListBuffer[JField]()
  def addNode(node: JField): JDecimalJObjectJsonWriter = {
    nodes += node
    this
  }
  def startArray(): JsonWriter[JValue] = {
    sys.error(
      "You have to start a field to be able to end it (startArray called before startField in a JObject builder)"
    )
  }

  def endArray(): JsonWriter[JValue] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  def startObject(): JsonWriter[JValue] =
    sys.error(
      "You have to start a field to be able to end it (startObject called before startField in a JObject builder)"
    )

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
    sys.error(
      "You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)"
    )

  def startField(name: String): JsonWriter[JValue] = new JDecimalJFieldJsonWriter(name, this)

  def addJValue(jv: JValue): JsonWriter[JValue] =
    sys.error(
      "You have to start a field to be able to end it (addJValue called before startField in a JObject builder)"
    )

  def result: JValue = JObject(nodes.toList: _*)
}

private final class JDoubleJArrayJsonWriter(parent: JsonWriter[JValue]) extends JDoubleAstJsonWriter {
  private[this] val nodes = ListBuffer[JValue]()
  def addNode(node: JValue): JsonWriter[JValue] = {
    nodes += node
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
    nodes += node
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

  def byte(value: Byte): JsonWriter[JValue] = addNode(JInt(value: Long))

  def int(value: Int): JsonWriter[JValue] = addNode(JInt(value))

  def long(value: Long): JsonWriter[JValue] = addNode(JInt(value))

  def bigInt(value: BigInt): JsonWriter[JValue] = addNode(JInt(value))

  def boolean(value: Boolean): JsonWriter[JValue] = addNode(JBool(value))

  def short(value: Short): JsonWriter[JValue] = addNode(JInt(value: Long))

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

  def bigDecimal(value: BigDecimal): JsonWriter[JValue] = addNode(JDouble(value.doubleValue))
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
