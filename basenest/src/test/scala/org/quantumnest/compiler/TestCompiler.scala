package org.quantumnest.compiler

import net.liftweb.util.Helpers;
import java.io.File

import org.junit._
import Assert._

@Test
class TestCompilerStuff {

  val prefix = "../examples/"
  val files =
    Vector("activity_pub/src/routes.json", "activity_pub/src/functions.json")

  @Test
  def testLoadsRoutes() {
    for (file <- files) {
      val filename = prefix + file
      val bytes = Helpers.readWholeFile(new File(filename))
      val str = new String(bytes)
      val source = CompilerArtifact
        .readArtifact(str)
        .openOrThrowException("This is a test")
      assertTrue(source != null)
    }
  }

}
