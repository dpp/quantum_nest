package org.quantumnest.quantum

import org.quantumnest.quantum._
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.util.Helpers;
import java.io.File
import org.quantumnest.compiler.CompilerArtifact
import org.quantumnest.util.Misc
import org.quantumnest.clojure.Util

class TestParsing extends munit.FunSuite {

  val prefix = "../examples/"
  val file = "activity_pub/src/routes.json"
  val json = {
    val filename = prefix + file
    val bytes = Helpers.readWholeFile(new File(filename))
    val str = new String(bytes)
    val source = CompilerArtifact
      .strToJson(str)
      .openOrThrowException("This is a test")
      source
  }

  test("Test State Definition Parsing") {
    val st = Quantum.extractState(Misc.randomUUIDBasedNamespace(), json)
    assert(st.isDefined)
    val opened = st.openOrThrowException("Already tested")
    val stateCnt = opened.length 
    assertEquals(stateCnt, 3)
    assertEquals(opened.map(_.openOrThrowException("should be Full")).filter(_.name == "user").head.name, "user")
  }

    test("Test State Definition execution") {
    val st = Quantum.extractState(Misc.randomUUIDBasedNamespace(), json)
    assert(st.isDefined)
    val opened = st.openOrThrowException("Already tested")
    val userInfo = opened.map(_.openOrThrowException("should be Full")).filter(_.name == "user-info").head
    val it = Util.clojureMap("body" -> 42)
    
    val res = userInfo.toExec.openOrThrowException("There should be code to execute").exec(Map("meow" -> it))
    assertEquals( res.openOrThrowException("should be 42"), 42.asInstanceOf[Object])
  }
}
