package org.quantumnest.clojure

import net.liftweb.util.Helpers;
import java.io.File
import net.liftweb.common.Full

class ClojureStuff extends munit.FunSuite {
  test("load and run clojure code") {
    Util.loadClojureResource("hello")

    val gotFoo = Util.eval("(yak-smell/foo)")

    assert(gotFoo == Full(42))

    val what = Util.compileCode("wombat", List(("dog", List("x"), "(* x 2)")))

    val res = what("dog").invoke(75)

    assert(res == 150)

    assert(Full(2) == Util.eval("(wombat/dog 1)"))
  }

  test("evaluate a Clojure expression") {
    val obtained = Util.eval("(+ 41 1)")
    val expected = Full(42L.asInstanceOf[Object])
    assertEquals(obtained, expected)
  }

  test("Create a symbol") {
    val symbol = Util.symbolFor("hello")

    assertEquals("hello", symbol.getName())
  }

}
