package org.json4s
package reflect

import org.json4s.scalap.scalasig._
import annotation.tailrec

object ScalaSigReader {
  def readConstructor(argName: String, clazz: Class[_], typeArgIndex: Int, argNames: List[String]): Class[_] = {
    val cl = findClass(clazz)
    val cstr = findConstructor(cl, argNames).getOrElse(fail("Can't find constructor for " + clazz))
    findArgType(cstr, argNames.indexOf(argName), typeArgIndex)
  }
  def readConstructor(argName: String, clazz: Class[_], typeArgIndexes: List[Int], argNames: List[String]): Class[_] = {
    val cl = findClass(clazz)
    val cstr = findConstructor(cl, argNames).getOrElse(fail("Can't find constructor for " + clazz))
    findArgType(cstr, argNames.indexOf(argName), typeArgIndexes)
  }

  def readConstructor(argName: String, clazz: ScalaType, typeArgIndex: Int, argNames: List[String]): Class[_] = {
    val cl = findClass(clazz.erasure)
    val cstr = findConstructor(cl, argNames).getOrElse(fail("Can't find constructor for " + clazz))
    findArgType(cstr, argNames.indexOf(argName), typeArgIndex)
  }

  def readConstructor(argName: String, clazz: ScalaType, typeArgIndexes: List[Int], argNames: List[String]): Class[_] = {
    val cl = findClass(clazz.erasure)
    val cstr = findConstructor(cl, argNames)

    val maybeArgType = cstr map { c =>
      findArgType(cstr.get, argNames.indexOf(argName), typeArgIndexes)
    } orElse {
      val companionClass = findCompanionObject(clazz.erasure)
      findApply(companionClass, argNames) map { methodSymbol =>
        findArgType(methodSymbol, argNames.indexOf(argName), typeArgIndexes)
      }
    }
    maybeArgType.getOrElse(fail("Can't find constructor for " + clazz))
  }

  def readField(name: String, clazz: Class[_], typeArgIndex: Int): Class[_] = {
    def read(current: Class[_]): MethodSymbol = {
      if (current == classOf[java.lang.Object])
        fail("Can't find field " + name + " from " + clazz)
      else
        findField(current, name)
          .orElse(current.getInterfaces.flatMap(findField(_, name)).headOption)
          .getOrElse(read(current.getSuperclass))
    }
    findArgTypeForField(read(clazz), typeArgIndex)
  }

  def findClass(clazz: Class[_]): ClassSymbol = {
    val sig = findScalaSig(clazz).getOrElse(fail("Can't find ScalaSig for " + clazz))
    findClass(sig, clazz).getOrElse(fail("Can't find " + clazz + " from parsed ScalaSig"))
  }

  def findClass(sig: ScalaSig, clazz: Class[_]): Option[ClassSymbol] = {
    val name = safeSimpleName(clazz)

    sig.symbols.collect { case c: ClassSymbol if !c.isModule => c }.find(_.name == name).orElse {
      sig.topLevelClasses.find(_.symbolInfo.name == name).orElse {
        sig.topLevelObjects.map { obj =>
          val t = obj.infoType.asInstanceOf[TypeRefType]
          t.symbol.children collect { case c: ClassSymbol => c } find(_.symbolInfo.name == name)
        }.head
      }
    }
  }

  def findCompanionObject(clazz: Class[_]): ClassSymbol = {
    val sig = findScalaSig(clazz).getOrElse(fail("Can't find ScalaSig for " + clazz))
    findCompanionObject(sig, clazz).getOrElse(fail("Can't find " + clazz + " from parsed ScalaSig"))
  }

  def findCompanionObject(sig: ScalaSig, clazz: Class[_]): Option[ClassSymbol] = {
    val name = safeSimpleName(clazz)
    sig.symbols.collect { case c: ClassSymbol if c.isModule => c }.find(_.name == name)
  }

  def findConstructor(c: ClassSymbol, argNames: List[String]): Option[MethodSymbol] = {
    val ms = c.children collect {
      case m: MethodSymbol if m.name == "<init>" => m
    }
    ms.find(m => m.children.map(_.name) == argNames)
  }

  def findApply(c: ClassSymbol, argNames: List[String]): Option[MethodSymbol] = {
    val ms = c.children collect {
      case m: MethodSymbol if m.name == "apply" => m
    }
    ms.find(m => m.children.map(_.name) == argNames)
  }

  def findFields(c: ClassSymbol): Seq[MethodSymbol] =
    c.children collect {
      case m: MethodSymbol if m.infoType.isInstanceOf[NullaryMethodType] && !m.isSynthetic => m
    }


  private def findField(clazz: Class[_], name: String): Option[MethodSymbol] = findField(findClass(clazz), name)

  private def findField(c: ClassSymbol, name: String): Option[MethodSymbol] =
    (c.children collect { case m: MethodSymbol if m.name == name => m }).headOption

  def findArgType(s: MethodSymbol, argIdx: Int, typeArgIndex: Int): Class[_] = {
    def findPrimitive(t: Type): Symbol = {
      t match {
        case TypeRefType(ThisType(_), symbol, _) if isPrimitive(symbol) => symbol
        case TypeRefType(_, _, TypeRefType(ThisType(_), symbol, _) :: xs) => symbol
        case TypeRefType(_, symbol, Nil) => symbol
        case TypeRefType(_, _, args) if typeArgIndex >= args.length =>
          findPrimitive(args(0))
        case TypeRefType(_, _, args) =>
          val ta = args(typeArgIndex)
          ta match {
            case ref @ TypeRefType(_, _, _) => findPrimitive(ref)
            case x => fail("Unexpected type info " + x)
          }
        case x => fail("Unexpected type info " + x)
      }
    }
    toClass(findPrimitive(s.children(argIdx).asInstanceOf[SymbolInfoSymbol].infoType))
  }

  def findArgType(s: MethodSymbol, argIdx: Int, typeArgIndexes: List[Int]): Class[_] = {
    @tailrec def findPrimitive(t: Type, curr: Int): Symbol = {
      val ii = (typeArgIndexes.length - 1) min curr
      t match {
        case TypeRefType(ThisType(_), symbol, _) if isPrimitive(symbol) => symbol
        case TypeRefType(_, symbol, Nil) => symbol
        case TypeRefType(_, _, args) if typeArgIndexes(ii) >= args.length =>
          findPrimitive(args(0 max args.length - 1), curr + 1)
        case TypeRefType(_, _, args) =>
          val ta = args(typeArgIndexes(ii))
          ta match {
            case ref @ TypeRefType(_, _, _) => findPrimitive(ref, curr + 1)
            case x => fail("Unexpected type info " + x)
          }
        case x => fail("Unexpected type info " + x)
      }
    }
    toClass(findPrimitive(s.children(argIdx).asInstanceOf[SymbolInfoSymbol].infoType, 0))
  }

  private def findArgTypeForField(s: MethodSymbol, typeArgIdx: Int): Class[_] = {
    val t = s.infoType match {
      case NullaryMethodType(TypeRefType(_, _, args)) => args(typeArgIdx)
    }

    def findPrimitive(t: Type): Symbol = t match {
      case TypeRefType(ThisType(_), symbol, _) => symbol
      case x => fail("Unexpected type info " + x)
    }
    toClass(findPrimitive(t))
  }

  private def toClass(s: Symbol) = s.path match {
    case "scala.Short"   => classOf[Short]
    case "scala.Int"     => classOf[Int]
    case "scala.Long"    => classOf[Long]
    case "scala.Boolean" => classOf[Boolean]
    case "scala.Float"   => classOf[Float]
    case "scala.Double"  => classOf[Double]
    case "scala.Byte"    => classOf[Byte]
    case _               => classOf[AnyRef]
  }

  private[this] def isPrimitive(s: Symbol) = toClass(s) != classOf[AnyRef]

  def findScalaSig(clazz: Class[_]): Option[ScalaSig] = try {
    // taken from ScalaSigParser parse method with the explicit purpose of walking away from NPE
    parseClassFileFromByteCode(clazz).orElse(findScalaSig(clazz.getDeclaringClass))
  } catch {
    case e: NullPointerException => None // yes, this is the exception, but it is totally unhelpful to the end user
  }

  private[this] def parseClassFileFromByteCode(clazz: Class[_]): Option[ScalaSig] =
    Option(ClassFileParser.parse(ByteCode.forClass(clazz))) flatMap ScalaSigParser.parse

  val ModuleFieldName = "MODULE$"
  val OuterFieldName = "$outer"
  val ClassLoaders = Vector(this.getClass.getClassLoader)

  def companions(t: String, companion: Option[AnyRef] = None, classLoaders: Iterable[ClassLoader] = ClassLoaders) = {
    def path(tt: String) = if (tt.endsWith("$")) tt else (tt + "$")
    val cc: Option[Class[_]] = resolveClass(path(t), classLoaders) flatMap ((c: Class[_]) => resolveClass(path(Reflector.rawClassOf(c).getName), classLoaders))
    def safeField(ccc: Class[_]) =
      try { Option(ccc.getField(ModuleFieldName)).map(_.get(companion.orNull)) } catch { case _: Throwable => None }
    cc map (ccc => (ccc, safeField(ccc)))
  }

  def resolveClass[X <: AnyRef](c: String, classLoaders: Iterable[ClassLoader] = ClassLoaders): Option[Class[X]] = {
      try {
        var clazz: Class[_] = null
        val iter = classLoaders.iterator ++ List(Thread.currentThread().getContextClassLoader())
        while (clazz == null && iter.hasNext) {
          try {
            clazz = Class.forName(c, true, iter.next())
          }
          catch {
            case e: ClassNotFoundException => // keep going, maybe it's in the next one
          }
        }

        if (clazz != null) Some(clazz.asInstanceOf[Class[X]]) else None
      }
      catch {
        case _: Throwable => None
      }
    }
}
