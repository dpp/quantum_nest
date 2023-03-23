package org.quantumnest.quantum

import org.quantumnest.quantum._
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.util.Helpers;
import java.io.File
import org.quantumnest.compiler.CompilerArtifact

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
    val st = Quantum.extractState(json)
    assert(st.isDefined)
    val opened = st.openOrThrowException("Already tested")
    val stateCnt = opened.length 
    assertEquals(stateCnt, 3)
    assertEquals(opened.filter(_.name == "user").head.name, "user")
  }
}
