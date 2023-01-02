package quantum_nest.clojure

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.java.api.Clojure;

object Util {
  lazy val ClojureRequire = RT.`var`("clojure.core", "require");
  lazy val ClojureEval = RT.`var`("clojure.core", "eval");


  def eval(s: String): Object = {
    val parsed = Clojure.read(s)
    ClojureEval.invoke(parsed)
  }

  def symbolFor(s: String): Symbol =
    Symbol.create(s)
}
