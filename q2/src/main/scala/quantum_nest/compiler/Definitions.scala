package quantum_nest.compiler

import net.liftweb.json.JsonAST
import net.liftweb.common.Box
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.util.Helpers
import net.liftweb.common.Empty
import net.liftweb.common.EmptyBox
import net.liftweb.common.Full
import net.liftweb.common.Failure
import quantum_nest.ClojureCompiler
import clojure.lang.IFn

case class Definitions(definitions: List[Definition]) extends CompilerArtifact {
  lazy val namespace = s"qn-${Helpers.randomString(15)}"

  def compile(): Box[Map[String, IFn]] = {
    Empty
  }
}

object Definitions {
  implicit val formats =
    Serialization.formats(
      ShortTypeHints(List(classOf[Definitions], classOf[Definition]))
    )
  def fromJson(json: JsonAST.JValue): Box[Definitions] = {

    val defs = json match {
      case JArray(arr) => {
        val definitions = arr.map(j => {
          Definition.fromJson(j) match {
            case Full(v)     => v
            case x: EmptyBox => return x
          }
        })

        definitions
      }
      case x =>
        return Failure(
          s"Failed to extract Definitions because ${x} is not an Array"
        )
    }

    Full(Definitions(defs))
  }
}

case class Definition(name: String, params: List[String], code: String)
    extends CompilerArtifact

object Definition {
  implicit val formats =
    Serialization.formats(ShortTypeHints(List(classOf[Definition])))
  def fromJson(json: JsonAST.JValue): Box[Definition] = {
    val ret = Helpers.tryo {
      json.extract[Definition]
    }

    ret

  }
}
