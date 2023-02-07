/*
 * This Kotlin source file was generated by the Gradle "init" task.
 */
package nest.app

import groovy.lang.Tuple3
import nest.clojure.ClUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClojureStuff {
    @Test
    fun testLoadHello() {
        ClUtil.loadClojureResource("hello")
    }

    @Test
    fun testFoo() {
        val gotFoo = ClUtil.eval("(yak-smell/foo)")
        assertEquals(gotFoo, 42L)
    }

    @Test
    fun testCompiler() {
        val toCompile = Tuple3("dog", ClUtil.buildList("x"), "(* x 2)")

        val what = ClUtil.compileCode("wombat", ClUtil.buildList(toCompile))

        val theFn = what.get("dog")

        val res = theFn.invoke(75)

        assertEquals(res, 150L)

        assertEquals(2L, ClUtil.eval("(wombat/dog 1)"))
    }

    @Test
    fun createSymbol() {
        val sym = ClUtil.symbolFor("hello")

        assertEquals(sym.getName(), "hello")
    }
}
