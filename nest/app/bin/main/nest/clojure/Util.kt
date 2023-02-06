package nest.clojure


import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.java.api.Clojure;
import clojure.lang.IFn
import groovy.lang.Tuple3
import java.util.ArrayList
import java.util.List as JList
import java.util.Map as JMap
import clojure.lang.PersistentList as ClList
import clojure.lang.PersistentHashMap as ClMap

// @Suppress("UNCHECKED_CAST")

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
object Util {
  val ClojureRequire: Var = RT.`var`("clojure.core", "require");
   val ClojureEval: Var = RT.`var`("clojure.core", "eval");
   val ClojureLoad: Var = RT.`var`("clojure.core", "load");
   val compiler: Var = bootstrapCompiler()

  fun bootstrapCompiler(): Var {
    loadClojureResource("hello")
    return RT.`var`("yak-smell", "compile-functions")
  }


  fun compileCode(
      namespace: String,
      code: JList<Tuple3<String, JList<String>, String>>
  ) : JMap<String, IFn>  {

    val theCode =  ArrayList<ArrayList<Object>>()
    for (i in code) {
      val inner = ArrayList<Object>()
      inner.add(i.v1 as Object)
      val params = i.v2 as Object
      inner.add(params)
      inner.add(i.v3 as Object)

      theCode.add(inner)
    }

    val c1 = compiler
      .invoke(
        namespace,
        theCode
      ) as JMap<String, IFn>

    return c1
  }

  fun eval(s: String): Any  {
    val parsed = Clojure.read(s)
    return ClojureEval.invoke(parsed)
  }

  fun loadClojureResource(resourceName: String): Any  {
    val ret = ClojureLoad.invoke(resourceName)
    return ret
  }

  fun symbolFor(s: String): Symbol {
    return Symbol.create(s)
  }
}
