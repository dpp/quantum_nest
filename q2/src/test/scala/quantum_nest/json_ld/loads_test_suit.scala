package quantum_nest.json_ld

import quantum_nest.clojure.Util;

class LoadsJsonLD extends munit.FunSuite {
  test("evaluate a Clojure expression") {
    val obtained = Util.eval("(+ 41 1)").asInstanceOf[java.lang.Long].intValue()
    val expected = 42
    assertEquals(obtained, expected)
  }

  test("Create a symbol") {
    val symbol = Util.symbolFor("hello")

    assertEquals("hello", symbol.getName())
  }

}
