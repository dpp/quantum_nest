import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.java.api.Clojure;

@main def hello: Unit = 
  println("Hello world!")
  println(msg)
  println("Eval "+ClojureEval)
  val question = Clojure.read("(+ 41 1)")
  println("Question "+question+" class "+question.getClass())
  println("Answer "+ClojureEval.invoke(question))

def msg = "I was compiled by Scala 3. :)"

lazy val ClojureRequire = RT.`var`("clojure.core", "require");
lazy val ClojureEval = RT.`var`("clojure.core", "eval");

lazy val Foo = 46 + "dogs"