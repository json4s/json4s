package org.json4s

import java.io.{ StringWriter, Writer => JWriter }

trait Builder[T] {
  def startArray(): Builder[T]
  def endArray(): Builder[T]
  def startObject(): Builder[T]
  def endObject(): Builder[T]
  def string(value: String): Builder[T]
  def byte(value: Byte): Builder[T]
  def int(value: Int): Builder[T]
  def long(value: Long): Builder[T]
  def bigInt(value: BigInt): Builder[T]
  def boolean(value: Boolean): Builder[T]
  def short(value: Short): Builder[T]
  def float(value: Float): Builder[T]
  def double(value: Double): Builder[T]
  def bigDecimal(value: BigDecimal): Builder[T]
  def startField(name: String): Builder[T]
  def result: T
}
private class JDoubleJFieldBuilder(name: String, parent: JDoubleJObjectBuilder) extends JDoubleAstBuilder {
  def result: JValue = JNothing

  @inline
  def addNode(node: JValue): Builder[JValue] = parent.addNode(name -> node)
}
private class JDoubleAstRootBuilder extends JDoubleAstBuilder {
  private[this] var nodes = List.empty[JValue]
      
  @inline
  def addNode(node: JValue): Builder[JValue] = {
    nodes ::= node
    this
  }
  def result: JValue = {
    if (nodes.nonEmpty) nodes.head else JNothing
  }
}
private class JDoubleJObjectBuilder(parent: Builder[JValue]) extends Builder[JValue] {
  private[this] var nodes = List.empty[JField]
  @inline
  def addNode(node: JField): JDoubleJObjectBuilder = {
    nodes ::= node
    this
  }
  def startArray(): Builder[JValue] = {
    sys.error("You have to start a field to be able to end it (startArray called before startField in a JObject builder)")
  }

  def endArray(): Builder[JValue] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  def startObject(): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (startObject called before startField in a JObject builder)")

  def endObject(): Builder[JValue] = {
    parent match {
      case p: JDoubleAstBuilder => p.addNode(result)
      case _ => parent
    }
  }

  def string(value: String): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (string called before startField in a JObject builder)")

  def byte(value: Byte): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (byte called before startField in a JObject builder)")

  def int(value: Int): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (int called before startField in a JObject builder)")

  def long(value: Long): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (long called before startField in a JObject builder)")

  def bigInt(value: BigInt): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (bigInt called before startField in a JObject builder)")

  def boolean(value: Boolean): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (boolean called before startField in a JObject builder)")

  def short(value: Short): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (short called before startField in a JObject builder)")

  def float(value: Float): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (float called before startField in a JObject builder)")

  def double(value: Double): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (double called before startField in a JObject builder)")

  def bigDecimal(value: BigDecimal): Builder[JValue] =
    sys.error("You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)")

  def startField(name: String): Builder[JValue] = new JDoubleJFieldBuilder(name, this)

  def result: JValue = JObject(nodes)
}
private class JDoubleJArrayBuilder(parent: Builder[JValue]) extends JDoubleAstBuilder {
  private[this] var nodes = List.empty[JValue]
  def addNode(node: JValue): Builder[JValue] = {
    nodes ::= node
    this
  }

  override def endArray(): Builder[JValue] = {
    parent match {
      case m: JDoubleAstBuilder => m.addNode(result)
      case _ => parent
    }
  }

  def result: JValue = JArray(nodes)
}
trait JValueBuilder extends Builder[JValue] {
  
  def addNode(node: JValue): Builder[JValue]
  
  def startArray(): Builder[JValue] = {
    new JDoubleJArrayBuilder(this)
  }

  def startObject(): Builder[JValue] = {
    new JDoubleJObjectBuilder(this)
  }

  def endObject(): Builder[JValue] = {
    sys.error("You have to start an object to be able to end it (endObject called before startObject)")
  }
  
  
  def startField(name: String): Builder[JValue] = {
    sys.error("You have to start an object before starting a field.")
  }
  
  def string(value: String): Builder[JValue] = addNode(JString(value))
  
  def byte(value: Byte): Builder[JValue] = addNode(JInt(value))
  
  def int(value: Int): Builder[JValue] = addNode(JInt(value))

  def long(value: Long): Builder[JValue] = addNode(JInt(value))

  def bigInt(value: BigInt): Builder[JValue] = addNode(JInt(value))

  def boolean(value: Boolean): Builder[JValue] = addNode(JBool(value))

  def short(value: Short): Builder[JValue] = addNode(JInt(value))

  def endArray(): Builder[JValue] = {
    sys.error("You have to start an object to be able to end it (endArray called before startArray)")
  }
  
}
trait JDoubleAstBuilder extends JValueBuilder {

  def float(value: Float): Builder[JValue] = addNode(JDouble(value))

  def double(value: Double): Builder[JValue] = addNode(JDouble(value))

  def bigDecimal(value: BigDecimal): Builder[JValue] = addNode(JDouble(value.doubleValue()))
}
class FieldStringOutputBuilder[T <: JWriter](name: String, isFirst: Boolean, protected[this] val nodes: T, protected[this] val level: Int, parent: ObjectStringOutputBuilder[T]) extends StringOutputBuilder[T] {
  def result: T = nodes


  override def startArray(): Builder[T] = {
    writeName()
    super.startArray()
  }

  override def startObject(): Builder[T] = {
    writeName()
    super.startObject()
  }

  private[this] def writeName() {
    if (!isFirst) nodes.write(",")
    nodes.append("\"")
    nodes.append(JsonAST.quote(name))
    nodes.append("\":")
  }

  def addNode(node: String): Builder[T] = {
    writeName()
    nodes.append(node)
    parent
  }

}
class ObjectStringOutputBuilder[T <: JWriter](protected[this] val nodes: T, protected[this] val level: Int, parent: StringOutputBuilder[T]) extends StringOutputBuilder[T] {
  nodes write '{'
  private[this] var isFirst = true
  def result: T = nodes

  def addNode(node: String): Builder[T] = {
    if (isFirst) isFirst = false
    else nodes.write(",")
    nodes write node
    this
  }

  override def endObject(): Builder[T] = {
    nodes.write('}')
    parent
  }
  
  override def startArray(): Builder[T] = {
    sys.error("You have to start a field to be able to end it (startArray called before startField in a JObject builder)")
  }

  override def endArray(): Builder[T] =
    sys.error("You have to start an array to be able to end it (endArray called before startArray)")

  override def startObject(): Builder[T] =
    sys.error("You have to start a field to be able to end it (startObject called before startField in a JObject builder)")
  
  
  override def string(value: String): Builder[T] =
    sys.error("You have to start a field to be able to end it (string called before startField in a JObject builder)")

  override def byte(value: Byte): Builder[T] =
    sys.error("You have to start a field to be able to end it (byte called before startField in a JObject builder)")

  override def int(value: Int): Builder[T] =
    sys.error("You have to start a field to be able to end it (int called before startField in a JObject builder)")

  override def long(value: Long): Builder[T] =
    sys.error("You have to start a field to be able to end it (long called before startField in a JObject builder)")

  override def bigInt(value: BigInt): Builder[T] =
    sys.error("You have to start a field to be able to end it (bigInt called before startField in a JObject builder)")

  override def boolean(value: Boolean): Builder[T] =
    sys.error("You have to start a field to be able to end it (boolean called before startField in a JObject builder)")

  override def short(value: Short): Builder[T] =
    sys.error("You have to start a field to be able to end it (short called before startField in a JObject builder)")

  override def float(value: Float): Builder[T] =
    sys.error("You have to start a field to be able to end it (float called before startField in a JObject builder)")

  override def double(value: Double): Builder[T] =
    sys.error("You have to start a field to be able to end it (double called before startField in a JObject builder)")

  override def bigDecimal(value: BigDecimal): Builder[T] =
    sys.error("You have to start a field to be able to end it (bigDecimal called before startField in a JObject builder)")

  override def startField(name: String): Builder[T] = {
    val r = new FieldStringOutputBuilder(name, isFirst, nodes, level, this)
    if (isFirst) isFirst = false
    r
  }
}
class ArrayStringOutputBuilder[T <: JWriter](protected[this] val nodes: T, protected[this] val level: Int, parent: StringOutputBuilder[T]) extends StringOutputBuilder[T] {
  nodes.write('[')
  private[this] var isFirst = true
  def result: T = nodes

  override def endArray(): Builder[T] = {
    nodes write ']'
    parent
  }

  private[this] def writeComma() {
    if (!isFirst) nodes.write(',')
    else isFirst = false
  }

  override def startArray(): Builder[T] = {
    writeComma()
    super.startArray()
  }

  override def startObject(): Builder[T] = {
    writeComma()
    super.startObject()
  }

  def addNode(node: String): Builder[T] = {
    println("isFirst in array builder: " + isFirst + " at level " + level)
    writeComma()
    nodes.write(node)
    this
  }
}
class RootStringOutputBuilder[T <: JWriter](protected[this] val nodes: T = new StringWriter()) extends StringOutputBuilder[T] {

  protected[this] val level: Int = 0

  def addNode(node: String): Builder[T] = {
    println("adding node to " + getClass.getSimpleName + " at level " + level)
    nodes write node
    this
  }


  def result: T = nodes
}
trait StringOutputBuilder[T <: JWriter] extends Builder[T] {

  protected[this] def level: Int
  protected[this] def nodes: T

  def startArray(): Builder[T] = {
    new ArrayStringOutputBuilder(nodes, level + 1, this)
  }

  def startObject(): Builder[T] = {
    new ObjectStringOutputBuilder(nodes, level + 1, this)
  }

  def addNode(node: String): Builder[T]

  def endObject(): Builder[T] = {
    sys.error("You have to start an object to be able to end it (endObject called before startObject)")
  }


  def startField(name: String): Builder[T] = {
    sys.error("You have to start an object before starting a field.")
  }

  def string(value: String): Builder[T] = addNode("\""+JsonAST.quote(value)+"\"")

  def byte(value: Byte): Builder[T] = addNode(value.toString)

  def int(value: Int): Builder[T] = addNode(value.toString)

  def long(value: Long): Builder[T] = addNode(value.toString)

  def bigInt(value: BigInt): Builder[T] = addNode(value.toString())

  def boolean(value: Boolean): Builder[T] = addNode(if (value) "true" else "false")

  def short(value: Short): Builder[T] = addNode(value.toString)

  def endArray(): Builder[T] = {
    sys.error("You have to start an object to be able to end it (endArray called before startArray)")
  }

  def float(value: Float): Builder[T] = addNode(value.toString)

  def double(value: Double): Builder[T] = addNode(value.toString)

  def bigDecimal(value: BigDecimal): Builder[T] = addNode(value.toString())

  def resultString: String = result.toString
}

