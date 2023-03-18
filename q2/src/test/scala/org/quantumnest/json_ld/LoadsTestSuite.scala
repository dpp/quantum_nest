package org.quantumnest.json_ld

import org.quantumnest.clojure.Util;
import org.junit._
import Assert._

@Test
class TestLoadsJsonLD  {

    @Test
    def testEvaluateClojureExpression() = {
    val obtained = Util.eval("(+ 41 1)").asInstanceOf[java.lang.Long].intValue()
    val expected = 42
    assertEquals(obtained, expected)
  }

  @Test
  def testCreateASymbol() = {
    val symbol = Util.symbolFor("hello")

    assertEquals("hello", symbol.getName())
  }

}

