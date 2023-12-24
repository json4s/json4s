package org.json4s

/**
 * Type hints can be used to alter the default conversion rules when converting
 * Scala instances into JSON and vice versa. Type hints must be used when converting
 * class which is not supported by default (for instance when class is not a case class).
 * <p>
 * Example:<pre>
 * class DateTime(val time: Long)
 *
 * val hints = new ShortTypeHints(classOf[DateTime] :: Nil) {
 *   override def serialize: PartialFunction[Any, JObject] = {
 *     case t: DateTime => JObject(JField("t", JInt(t.time)) :: Nil)
 *   }
 *
 *   override def deserialize: PartialFunction[(String, JObject), Any] = {
 *     case ("DateTime", JObject(JField("t", JInt(t)) :: Nil)) => new DateTime(t.longValue)
 *   }
 * }
 * implicit val formats: Formats = DefaultFormats.withHints(hints)
 * </pre>
 */
trait TypeHints {

  val hints: List[Class[?]]

  /**
   * Return hint for given type.
   */
  def hintFor(clazz: Class[?]): Option[String]

  /**
   * Return type for given hint.
   */
  def classFor(hint: String, parent: Class[?]): Option[Class[?]]

  /**
   * The name of the field in JSON where type hints are added (jsonClass by default)
   */
  def typeHintFieldName: String = "jsonClass"

  def isTypeHintField(f: JField, parent: Class[?]): Boolean = f match {
    case (key, JString(value)) =>
      val hint = typeHintFieldNameForHint(value, parent)
      key == typeHintFieldName && hint.isDefined
    case _ => false
  }
  def typeHintFieldNameForHint(hint: String, parent: Class[?]): Option[String] =
    classFor(hint, parent) map (_ => typeHintFieldName)
  def typeHintFieldNameForClass(clazz: Class[?]): Option[String] =
    hintFor(clazz).flatMap(typeHintFieldNameForHint(_, clazz))
  def containsHint(clazz: Class[?]): Boolean =
    hints exists (_ isAssignableFrom clazz)
  def shouldExtractHints(clazz: Class[?]): Boolean =
    hints exists (clazz isAssignableFrom _)
  def deserialize: PartialFunction[(String, JObject), Any] = Map()
  def serialize: PartialFunction[Any, JObject] = Map()

  def components: List[TypeHints] = List(this)

  /**
   * Adds the specified type hints to this type hints.
   */
  def +(hints: TypeHints): TypeHints = TypeHints.CompositeTypeHints(hints.components ::: components)

}

private[json4s] object TypeHints {

  private case class CompositeTypeHints(override val components: List[TypeHints]) extends TypeHints {
    val hints: List[Class[?]] = components.flatMap(_.hints)

    /**
     * Chooses most specific class.
     */
    def hintFor(clazz: Class[?]): Option[String] = {
      (components.reverse
      filter (_.containsHint(clazz))
      map { th =>
        val hint = th.hintFor(clazz)
        (
          hint,
          hint
            .flatMap(th.classFor(_, clazz))
            .getOrElse(
              sys.error("hintFor/classFor not invertible for " + th)
            )
        )
      }
      sortWith ((x, y) => (ClassDelta.delta(x._2, clazz) - ClassDelta.delta(y._2, clazz)) <= 0)).headOption
        .flatMap(_._1)
    }

    def classFor(hint: String, parent: Class[?]): Option[Class[?]] = {
      def hasClass(h: TypeHints) =
        scala.util.control.Exception.allCatch opt h.classFor(hint, parent) exists (_.isDefined)

      components find hasClass flatMap (_.classFor(hint, parent))
    }

    override def isTypeHintField(f: JField, parent: Class[?]): Boolean =
      components exists (_.isTypeHintField(f, parent))

    override def typeHintFieldNameForHint(hint: String, parent: Class[?]): Option[String] =
      components.flatMap(_.typeHintFieldNameForHint(hint, parent)).headOption

    override def typeHintFieldNameForClass(clazz: Class[?]): Option[String] =
      components.flatMap(_.typeHintFieldNameForClass(clazz)).headOption

    override def deserialize: PartialFunction[(String, JObject), Any] =
      components.foldLeft[PartialFunction[(String, JObject), Any]](Map()) { (result, cur) =>
        result.orElse(cur.deserialize)
      }

    override def serialize: PartialFunction[Any, JObject] =
      components.foldLeft[PartialFunction[Any, JObject]](Map()) { (result, cur) =>
        result.orElse(cur.serialize)
      }
  }

}
