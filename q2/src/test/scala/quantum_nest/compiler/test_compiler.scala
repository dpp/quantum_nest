package quantum_nest.compiler
import net.liftweb.util.Helpers;
import java.io.File

class CompilerStuff extends munit.FunSuite {
  val prefix = "../examples/"
  val files =
    Vector("activity_pub/src/routes.json", "activity_pub/src/functions.json")

  test("loads routes") {
    for (file <- files) {
      val filename = prefix + file
      val bytes = Helpers.readWholeFile(new File(filename))
      val str = new String(bytes)
      val source = CompilerArtifact
        .readArtifact(str)
        .openOrThrowException("This is a test")
      assert(source != null)
    }
  }

}
