package org.quantumnest.clojure

import net.liftweb.util.Helpers;
import java.io.File
import org.quantumnest.clojure.Util

import org.junit._
import Assert._

@Test
class TestClojureStuff {

  @Test
  def testClojureStuff() {
    Util.loadClojureResource("hello")

    val gotFoo = Util.eval("(yak-smell/foo)")

    assert(gotFoo == 42)

    val what = Util.compileCode("wombat", List(("dog", List("x"), "(* x 2)")))

    val res = what("dog").invoke(75)

    assert(res == 150)

    assert(2 == Util.eval("(wombat/dog 1)"))
  }
}
