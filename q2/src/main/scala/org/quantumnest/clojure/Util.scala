package org.quantumnest.clojure

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.java.api.Clojure;
import clojure.lang.IFn
import scala.jdk.CollectionConverters._
import net.liftweb.util.Helpers
import net.liftweb.common.Box
import clojure.lang.{ISeq, Keyword}
import clojure.lang.PersistentList
import clojure.lang.IPersistentList
import clojure.lang.IPersistentCollection
import clojure.lang.PersistentVector
import clojure.lang.PersistentHashMap
import clojure.lang.IPersistentMap
import net.liftweb.json.JsonAST
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import net.liftweb.common.Full
import org.quantumnest.util.Misc 

object Util {
  lazy val ClojureRequire = RT.`var`("clojure.core", "require");
  lazy val ClojureEval = RT.`var`("clojure.core", "eval");
  lazy val ClojureLoad = RT.`var`("clojure.core", "load");
  lazy val compiler: Var = bootstrapCompiler()

  def bootstrapCompiler(): Var = {
    loadClojureResource("hello")
    RT.`var`("yak-smell", "compile-functions")
  }

  def compileCode(
      namespace: String,
      code: Seq[(String, Seq[String], String)]
  ): Box[Map[String, IFn]] = {

    val theCode: java.util.List[java.util.List[Object]] =
      new java.util.ArrayList()
    for (i <- code) {
      val inner: java.util.List[Object] = new java.util.ArrayList()
      inner.add(i._1)
      val params: java.util.List[String] = i._2.asJava
      inner.add(params)
      inner.add(i._3)

      theCode.add(inner)
    }
    Helpers.tryo(
      compiler
        .invoke(
          namespace,
          theCode
        )
        .asInstanceOf[java.util.Map[String, IFn]]
        .asScala
        .toMap
    )

  }

  /** Evaluate a String that represents a Clojure expression
    *
    * @param s
    *   the Clojure expression
    * @return
    *   the value rendered by the expression
    */
  def eval(s: String): Box[Object] = {
    val parsed = Clojure.read(s)
    Helpers.tryo(ClojureEval.invoke(parsed))
  }

  def loadClojureResource(resourceName: String): Object = {
    val ret = ClojureLoad.invoke(resourceName)
    ret
  }

  def keywordFor(s: String): Keyword =
    Keyword.intern(s)

    /** Create a persistent list of items from a Scala seq
      *
      * @param in
      *   the list of objects to turn into a Clojure collection (List)
      * @return
      *   a Clojure collection (list)
      */
  def toClojureVector[A](in: Seq[A]): IPersistentCollection = {
    in.foldLeft[IPersistentCollection](clojureEmptyVector()) { (lst, item) =>
      append(lst, item)
    }
  }

  def clojureEmptyVector(): PersistentVector = PersistentVector.EMPTY

  def append(lst: IPersistentCollection, item: Any): IPersistentCollection = {
    lst.cons(item)
  }

  def clojureEmptyMap(): IPersistentMap = PersistentHashMap.EMPTY

  /** Appends a key/value pair to a Clojure PersistentMap
    *
    * @param map
    *   the incoming `Map`
    * @param key
    *   the key to set
    * @param value
    *   the value to set
    * @return
    *   the updated map
    */
  def appendToMap(map: IPersistentMap, key: Any, value: Any): IPersistentMap = {
    map.assoc(key, value)
  }

  def appendSymKeyToMap(
      map: IPersistentMap,
      key: Any,
      value: Any
  ): IPersistentMap = {
    map.assoc(
      key match {
        case s: Symbol => s
        case s: String => keywordFor(s)
        case null      => keywordFor("null")
        case s         => appendSymKeyToMap(map, key.toString(), value)
      },
      value
    )
  }

  def appendSymKeysToMap(
      map: IPersistentMap,
      pairs: (Any, Any)*
  ): IPersistentMap = {
    pairs.foldLeft(map) { case (m, (k, v)) => appendSymKeyToMap(m, k, v) }
  }

  def clojureMap(pairs: (Any, Any)*): IPersistentMap =
    appendSymKeysToMap(clojureEmptyMap(), pairs: _*)
}

case class ExecFunction(func: IFn, variables: Vector[String]) {
  def exec(in: Map[String, Any]): Box[Object] = {

    val toPass: Vector[Any] = variables.map(v => in.get(v).getOrElse(null))
    Helpers.tryo(toPass match {
      case Vector()                       => func.invoke()
      case Vector(a)                      => func.invoke(a)
      case Vector(a, b)                   => func.invoke(a, b)
      case Vector(a, b, c)                => func.invoke(a, b, c)
      case Vector(a, b, c, d)             => func.invoke(a, b, c, d)
      case Vector(a, b, c, d, e)          => func.invoke(a, b, c, d, e)
      case Vector(a, b, c, d, e, f)       => func.invoke(a, b, c, d, e, f)
      case Vector(a, b, c, d, e, f, g)    => func.invoke(a, b, c, d, e, f, g)
      case Vector(a, b, c, d, e, f, g, h) => func.invoke(a, b, c, d, e, f, g, h)
      case _ => func.applyTo(Util.toClojureVector(toPass).seq)
    })
  }
}

object ExecFunction {
  def fromJObject(
      namespace: String,
      obj: JsonAST.JField,
      variables: Vector[String]
  ): Box[ExecFunction] = {
    if (obj.name == "exec") {
      obj.value match {
        case JsonAST.JString(toExec) => {
          val funcName = Misc.randomUUIDBasedNamespace()
          val code =
            Util.compileCode(namespace, List((funcName, variables, toExec)))
          val theFn = code.flatMap(v => v.get(funcName))

          theFn match {
            case Empty            => Empty
            case v: Failure       => v
            case Full(compiledFn) => Full(ExecFunction(compiledFn, variables))
          }
        }
        case v => Failure(f"Could not create an execution block with ${v}")
      }
    } else
      Empty
  }
}
