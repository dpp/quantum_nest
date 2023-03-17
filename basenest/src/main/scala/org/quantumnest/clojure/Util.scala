package org.quantumnest.clojure

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.java.api.Clojure;
import clojure.lang.IFn
import scala.jdk.CollectionConverters._

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
      code: List[(String, List[String], String)]
  ): Map[String, IFn] = {

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
    compiler
      .invoke(
        namespace,
        theCode
      )
      .asInstanceOf[java.util.Map[String, IFn]]
      .asScala
      .toMap

  }

  def eval(s: String): Object = {
    val parsed = Clojure.read(s)
    ClojureEval.invoke(parsed)
  }

  def loadClojureResource(resourceName: String): Object = {
    val ret = ClojureLoad.invoke(resourceName)
    ret
  }

  def symbolFor(s: String): Symbol =
    Symbol.create(s)
}
