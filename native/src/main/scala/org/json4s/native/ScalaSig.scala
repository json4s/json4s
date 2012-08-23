/*
 * Copyright 2009-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s

import scala.tools.scalap.scalax.rules.scalasig._

private[json4s] object ScalaSigReader {
  def readConstructor(argName: String, clazz: Class[_], typeArgIndex: Int, argNames: List[String]): Class[_] = {
    val cl = findClass(clazz)
    val cstr = findConstructor(cl, argNames).getOrElse(Meta.fail("Can't find constructor for " + clazz))
    findArgType(cstr, argNames.indexOf(argName), typeArgIndex)
  }

  def readField(name: String, clazz: Class[_], typeArgIndex: Int): Class[_] = {
    def read(current: Class[_]): MethodSymbol = {
      if (current == null) 
        Meta.fail("Can't find field " + name + " from " + clazz)
      else
        findField(findClass(current), name).getOrElse(read(current.getSuperclass))
    }
    findArgTypeForField(read(clazz), typeArgIndex)
  }

  private def findClass(clazz: Class[_]): ClassSymbol = {
    val sig = findScalaSig(clazz).getOrElse(Meta.fail("Can't find ScalaSig for " + clazz))
    findClass(sig, clazz).getOrElse(Meta.fail("Can't find " + clazz + " from parsed ScalaSig"))
  }

  private def findClass(sig: ScalaSig, clazz: Class[_]): Option[ClassSymbol] = {
    sig.symbols.collect { case c: ClassSymbol if !c.isModule => c }.find(_.name == clazz.getSimpleName).orElse {
      sig.topLevelClasses.find(_.symbolInfo.name == clazz.getSimpleName).orElse {
        sig.topLevelObjects.map { obj => 
          val t = obj.infoType.asInstanceOf[TypeRefType]
          t.symbol.children collect { case c: ClassSymbol => c } find(_.symbolInfo.name == clazz.getSimpleName) 
        }.head
      }
    }
  }

  private def findConstructor(c: ClassSymbol, argNames: List[String]): Option[MethodSymbol] = {
    val ms = c.children collect { case m: MethodSymbol if m.name == "<init>" => m }
    ms.find(m => m.children.map(_.name) == argNames)
  }

  private def findField(c: ClassSymbol, name: String): Option[MethodSymbol] = 
    (c.children collect { case m: MethodSymbol if m.name == name => m }).headOption

  private def findArgType(s: MethodSymbol, argIdx: Int, typeArgIndex: Int): Class[_] = {
    def findPrimitive(t: Type): Symbol = t match { 
      case TypeRefType(ThisType(_), symbol, _) if isPrimitive(symbol) => symbol
      case TypeRefType(_, _, TypeRefType(ThisType(_), symbol, _) :: xs) => symbol
      case TypeRefType(_, symbol, Nil) => symbol
      case TypeRefType(_, _, args) if typeArgIndex >= args.length => findPrimitive(args(0))
      case TypeRefType(_, _, args) =>
        args(typeArgIndex) match {
          case ref @ TypeRefType(_, _, _) => findPrimitive(ref)
          case x => Meta.fail("Unexpected type info " + x)
        }
      case x => Meta.fail("Unexpected type info " + x)
    }
    toClass(findPrimitive(s.children(argIdx).asInstanceOf[SymbolInfoSymbol].infoType))
  }

  private def findArgTypeForField(s: MethodSymbol, typeArgIdx: Int): Class[_] = {
    // FIXME can be removed when 2.8 no longer needs to be supported.
    // 2.8 does not have NullaryMethodType, work around that.
    /*
    val t = s.infoType match {
      case NullaryMethodType(TypeRefType(_, _, args)) => args(typeArgIdx)
    }
    */
    def resultType = try {
      s.infoType.asInstanceOf[{ def resultType: Type }].resultType
    } catch {
      case e: java.lang.NoSuchMethodException => s.infoType.asInstanceOf[{ def typeRef: Type }].typeRef
    }

    val t = resultType match {
      case TypeRefType(_, _, args) => args(typeArgIdx)
    }

    def findPrimitive(t: Type): Symbol = t match { 
      case TypeRefType(ThisType(_), symbol, _) => symbol
      case ref @ TypeRefType(_, _, _) => findPrimitive(ref)
      case x => Meta.fail("Unexpected type info " + x)
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
    case _               => classOf[AnyRef]
  }

  private def isPrimitive(s: Symbol) = toClass(s) != classOf[AnyRef]

  private def findScalaSig(clazz: Class[_]): Option[ScalaSig] = 
    ScalaSigParser.parse(clazz).orElse(findScalaSig(clazz.getDeclaringClass))
}
