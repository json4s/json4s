package org.json4s

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

  def addNode(node: JValue): Builder[JValue] = parent.addNode(name -> node)
}
private class JDoubleAstRootBuilder extends JDoubleAstBuilder {
  private[this] var nodes = List.empty[JValue]
      
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
      case p: JDoubleJFieldBuilder => p.addNode(result)
      case p: JDoubleAstBuilder => p.addNode(result)
    }
    parent
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
