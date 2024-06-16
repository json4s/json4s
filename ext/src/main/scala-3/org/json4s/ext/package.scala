package org.json4s

package object ext {
  /* This a hack to extract the `Value` class member of a particular `Enumeration`.
   * To be compatible with SIP-56 match types, the type member capture `type Value = A`
   * must correspond to a type member of the "parent" of the refinement.
   * Usually the parent should be a class, and we would expect
   *
   *   type Aux[A] = Enumeration { type Value }
   *
   * However, that is not a legal definition in the first place, because
   *
   * > type Value cannot have the same name as class Value in class Enumeration
   * > -- class definitions cannot be overridden
   *
   * Therefore, we cheat, and we use a structural *definition* { type Value }
   * for the parent, which we immediately *refine* as { type Value = A }.
   * The definition in the parent specifies that the declared bounds for Value
   * are >: Nothing <: Any, which allows the match type to know that its type
   * capture `a` has those bounds.
   *
   * ---
   *
   * The actual *clean* way of doing all of this would be *not* to use an
   * `EnumValue[E]`, but instead use a `val enumeration: E` and the
   * path-dependent type `enumeration.Value`. However, that would require a
   * change of public API.
   */
  private type Aux[A] = ({ type Value }) { type Value = A }

  private[ext] type EnumValue[A <: Enumeration] = A match {
    case Aux[a] => a
  }
}
