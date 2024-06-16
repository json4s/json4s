package org.json4s
package reflect

import scala.annotation.tailrec
import scala.quoted._
import scala.reflect.NameTransformer

object ScalaSigReader {
  private[this] val localPathMemo = new Memo[String, Option[Class[?]]]
  private[this] val remotePathMemo = new Memo[(String, Iterable[ClassLoader]), Option[Class[?]]]

  def readConstructor(argName: String, clazz: ScalaType, typeArgIndex: Int, argNames: List[String]): Class[?] = {
    given staging.Compiler = staging.Compiler.make(this.getClass.getClassLoader)

    staging.withQuotes {
      import quotes.reflect._
      val cl = Symbol.classSymbol(clazz.erasure.getCanonicalName())
      val cstr =
        findConstructor(cl, argNames)
          .orElse {
            val companionClass = cl.companionModule
            companionClass.methodMembers
              .collect {
                case m: Symbol if m.name == "apply" => m
              }
              .find((m: Symbol) => getFirstTermList(m).map(_.map(_.name) == argNames).getOrElse(false))
          }
          .getOrElse(
            fail("Can't find constructor for " + clazz)
          )
      findArgType(using quotes)(cstr, argNames.indexOf(argName), typeArgIndex)
    }
  }

  def readConstructor(
    argName: String,
    clazz: ScalaType,
    typeArgIndexes: List[Int],
    argNames: List[String]
  ): Class[?] = {
    given staging.Compiler = staging.Compiler.make(ScalaSigReader.getClass.getClassLoader)
    staging.withQuotes {
      import quotes.reflect._
      val cl = Symbol.classSymbol(clazz.erasure.getCanonicalName())

      val cstr = findConstructor(cl, argNames)
      val maybeArgType = cstr map { _ =>
        findArgType(using quotes)(cstr.get, argNames.indexOf(argName), typeArgIndexes)
      } orElse {
        val companionClass = cl.companionModule
        findApply(companionClass, argNames) map { methodSymbol =>
          findArgType(using quotes)(methodSymbol, argNames.indexOf(argName), typeArgIndexes)
        }
      }
      maybeArgType.getOrElse(fail("Can't find constructor for " + clazz))
    }
  }

  def readField(name: String, clazz: Class[?], typeArgIndex: Int): Class[?] = {
    given staging.Compiler = staging.Compiler.make(this.getClass.getClassLoader)

    val nameWithSymbols = NameTransformer.decode(name)

    staging.withQuotes {
      import quotes.reflect._
      val sym = Symbol.classSymbol(
        clazz.getCanonicalName()
      )
      val methodSymbolMaybe =
        sym.fieldMembers
          .filter(_.name == nameWithSymbols)
          .headOption
      findArgTypeForField(methodSymbolMaybe.get, typeArgIndex)
    }
  }

  private def findConstructor(using
    quotes: Quotes
  )(
    cl: quotes.reflect.Symbol,
    argNames: List[String]
  ): Option[quotes.reflect.Symbol] = {
    import quotes.reflect._
    val argNamesWithSymbols = argNames.map(replaceWithSymbols(_))
    (cl.methodMembers :+ cl.primaryConstructor)
      .filter(_.isClassConstructor)
      .filter { m =>
        getFirstTermList(m) match {
          case None => false
          case Some(termList) =>
            termList.map(_.name).sameElements(argNamesWithSymbols)
        }
      }
      .headOption
  }

  private def findApply(using
    quotes: Quotes
  )(c: quotes.reflect.Symbol, argNames: List[String]): Option[quotes.reflect.Symbol] = {
    import quotes.reflect._
    val argNamesWithSymbols = argNames.map(replaceWithSymbols(_))

    val ms = c.methodMembers.collect {
      case m: Symbol if m.name == "apply" => m
    }

    ms.find(m => m.paramSymss(0).map(_.name) == argNamesWithSymbols) // TODO type parameters
  }

  private def findArgType(using quotes: Quotes)(
    s: quotes.reflect.Symbol,
    argIdx: Int,
    typeArgIndex: Int
  ): Class[?] = {
    import quotes.reflect._
    def findPrimitive(t: TypeRepr): Symbol = {
      def throwError() = fail("Unexpected type info " + t.show)
      if defn.ScalaPrimitiveValueClasses.contains(t.typeSymbol) then {
        t.typeSymbol
      } else if t.typeArgs.isEmpty then {
        t.typeSymbol // TODO investigate when this rhs should not be accessed
      } else
        t match {
          case AppliedType(_, typeArgs) if typeArgs.size <= typeArgIndex =>
            findPrimitive(typeArgs(0))
          case AppliedType(_, typeArgs) =>
            findPrimitive(typeArgs(typeArgIndex))
          case _ => throwError()
        }
    }
    toClass(
      findPrimitive(
        getFirstTermList(s) match {
          case None => fail("Incorrect function signature " + s)
          case Some(termList) =>
            s.typeRef.memberType(termList(argIdx))
        }
      )
    )
  }

  private def findArgType(using quotes: Quotes)(
    s: quotes.reflect.Symbol,
    argIdx: Int,
    typeArgIndexes: List[Int]
  ): Class[?] = {
    import quotes.reflect._
    @tailrec def findPrimitive(t: TypeRepr, curr: Int): Symbol = {
      import quotes.reflect._
      val ii = (typeArgIndexes.length - 1) min curr
      if defn.ScalaPrimitiveValueClasses.contains(t.typeSymbol) then {
        t.typeSymbol
      } else
        t match {
          case AppliedType(_, typeArgs) if typeArgIndexes(ii) >= typeArgs.length =>
            findPrimitive(typeArgs(0 max typeArgs.length - 1), curr + 1)
          case AppliedType(_, typeArgs) =>
            val ta = typeArgs(typeArgIndexes(ii))
            findPrimitive(ta, curr + 1)
          case x: TypeRef => x.typeSymbol
          case x => fail("Unexpected type info " + x)
        }
    }

    toClass(
      findPrimitive(
        getFirstTermList(s) match {
          case None => fail("Incorrect function signature " + s)
          case Some(termList) => s.typeRef.memberType(termList(argIdx))
        },
        0
      )
    )
  }

  private def findArgTypeForField(using
    quotes: Quotes
  )(methodSymbol: quotes.reflect.Symbol, typeArgIdx: Int) = {
    import quotes.reflect._
    val t =
      methodSymbol.owner.typeRef.memberType(methodSymbol).widen.dealias match {
        case MethodType(paramNames, paramTypes, retTpe) =>
          paramTypes(typeArgIdx)
        case AppliedType(_, args) =>
          // will be entered when methodSymbol is a var
          args(typeArgIdx)
      }

    toClass(t.typeSymbol)
  }

  private def toClass(using quotes: Quotes)(
    s: quotes.reflect.Symbol
  ): Class[?] = {
    import quotes.reflect._
    s.fullName match {
      case "scala.Short" => classOf[Short]
      case "scala.Int" => classOf[Int]
      case "scala.Long" => classOf[Long]
      case "scala.Boolean" => classOf[Boolean]
      case "scala.Float" => classOf[Float]
      case "scala.Double" => classOf[Double]
      case "scala.Byte" => classOf[Byte]
      case _ => classOf[AnyRef]
    }
  }

  private def getFirstTermList(using
    quotes: Quotes
  )(methodSymbol: quotes.reflect.Symbol): Option[List[quotes.reflect.Symbol]] = {
    import quotes.reflect._
    if methodSymbol.paramSymss(0).headOption.map(_.isTerm).getOrElse(false) then { // def method(arg...)
      Some(methodSymbol.paramSymss(0))
    } else if methodSymbol.paramSymss.length == 1 then {
      None
    } else if methodSymbol.paramSymss(1).headOption.map(_.isTerm).getOrElse(false) then { // def method[T...](arg...)
      Some(methodSymbol.paramSymss(1))
    } else
      None
  }

  private def replaceWithSymbols(name: String) = {
    scala.reflect.NameTransformer.decode(name)
  }

  val OuterFieldName = "$outer" // TODO should not be used, not sure what it does/means
  val ClassLoaders = Vector(this.getClass.getClassLoader, Thread.currentThread().getContextClassLoader)

  def companions(
    t: String,
    companion: Option[AnyRef] = None,
    classLoaders: Iterable[ClassLoader] = ClassLoaders
  ): Option[(Class[?], Option[AnyRef])] = {
    def path(tt: String) = if tt.endsWith("$") then tt else tt + "$"
    val cc: Option[Class[?]] = resolveClass(path(t), classLoaders) flatMap ((c: Class[?]) =>
      resolveClass(path(Reflector.rawClassOf(c).getName), classLoaders)
    )
    def safeField(ccc: Class[?]) =
      try { Option(ccc.getField(NameTransformer.MODULE_INSTANCE_NAME)).map(_.get(companion.orNull)) }
      catch { case _: Throwable => None }
    cc map (ccc => (ccc, safeField(ccc)))
  }

  def resolveClass[X <: AnyRef](c: String, classLoaders: Iterable[ClassLoader] = ClassLoaders): Option[Class[X]] = {
    if classLoaders eq ClassLoaders then {
      localPathMemo(c, c => resolveClassCached(c, classLoaders)).asInstanceOf[Option[Class[X]]]
    } else {
      remotePathMemo((c, classLoaders), tuple => resolveClassCached(tuple._1, tuple._2)).asInstanceOf[Option[Class[X]]]
    }
  }

  private def resolveClassCached[X <: AnyRef](c: String, classLoaders: Iterable[ClassLoader]): Option[Class[X]] = {
    try {
      var clazz: Class[?] = null
      val iter = classLoaders.iterator ++ Iterator.single(Thread.currentThread().getContextClassLoader)
      while clazz == null && iter.hasNext do {
        try {
          clazz = Class.forName(c, true, iter.next())
        } catch {
          case _: ClassNotFoundException => // keep going, maybe it's in the next one
        }
      }

      if clazz != null then Some(clazz.asInstanceOf[Class[X]]) else None
    } catch {
      case _: Throwable => None
    }
  }
}
