package org.json4s
package reflect

import java.lang.reflect.Field

sealed abstract class Descriptor extends Product with Serializable

case class PropertyDescriptor(name: String, mangledName: String, returnType: ScalaType, field: Field)
  extends Descriptor {

  def set(receiver: Any, value: Any) = field.set(receiver, value)
  def get(receiver: AnyRef) = field.get(receiver)
}

case class ConstructorParamDescriptor(
  name: String,
  mangledName: String,
  argIndex: Int,
  argType: ScalaType,
  defaultValue: Option[() => Any]
) extends Descriptor {

  lazy val isOptional = argType.isOption
  lazy val hasDefault = defaultValue.isDefined
}

case class ConstructorDescriptor(params: Seq[ConstructorParamDescriptor], constructor: Executable, isPrimary: Boolean)
  extends Descriptor

case class SingletonDescriptor(
  simpleName: String,
  fullName: String,
  erasure: ScalaType,
  instance: AnyRef,
  properties: Seq[PropertyDescriptor]
) extends Descriptor

sealed abstract class ObjectDescriptor extends Descriptor

case class ClassDescriptor(
  simpleName: String,
  fullName: String,
  erasure: ScalaType,
  companion: Option[SingletonDescriptor],
  constructors: Seq[ConstructorDescriptor],
  properties: Seq[PropertyDescriptor]
) extends ObjectDescriptor {

  def bestMatching(argNames: List[String]): Option[ConstructorDescriptor] = {
    case class Score(detailed: Int, optionalCount: Int, defaultCount: Int) {
      def isBetterThan(other: Score) = {
        (this.detailed == other.detailed && (this.optionalCount < other.optionalCount)) ||
        (this.detailed == other.detailed && (this.defaultCount > other.defaultCount)) ||
        this.detailed > other.detailed
      }
    }

    val names = Set(argNames: _*)
    def score(args: List[ConstructorParamDescriptor]): Score =
      Score(
        detailed = args.foldLeft(0)((s, arg) =>
          if (names.contains(arg.name)) s + 1
          else if (arg.isOptional) s
          else if (arg.hasDefault) s
          else -100
        ),
        optionalCount = args.count(_.isOptional),
        defaultCount = args.count(_.hasDefault)
      )

    if (constructors.isEmpty) None
    else {
      val best = constructors.tail.foldLeft((constructors.head, score(constructors.head.params.toList))) {
        (best: (ConstructorDescriptor, Score), c: ConstructorDescriptor) =>
          val newScore: Score = score(c.params.toList)
          if (newScore.isBetterThan(best._2)) (c, newScore) else best
      }
      Some(best._1)
    }
  }

  private[this] var _mostComprehensive: Seq[ConstructorParamDescriptor] = null

  def mostComprehensive: Seq[ConstructorParamDescriptor] = {
    if (_mostComprehensive == null)
      _mostComprehensive = if (constructors.nonEmpty) {
        val primaryCtors = constructors.filter(_.isPrimary)

        if (primaryCtors.length > 1) {
          throw new IllegalArgumentException(s"Two constructors annotated with PrimaryConstructor in `$fullName`")
        }

        primaryCtors.headOption
          .orElse(constructors.sortBy(-_.params.size).headOption)
          .map(_.params)
          .getOrElse(Nil)
      } else {
        Nil
      }

    _mostComprehensive
  }
}

case class PrimitiveDescriptor(erasure: ScalaType, default: Option[() => Any] = None) extends ObjectDescriptor
