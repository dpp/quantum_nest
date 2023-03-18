package org.quantumnest.compiler


import net.liftweb.common.{Box, Full, EmptyBox, Empty,  Failure}
import net.liftweb.util.Helpers
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.common.EmptyBox

/** A marker trait for all the artifacts
  */
trait CompilerArtifact {}

object CompilerArtifact {
  def readArtifact(str: String): Box[CompilerArtifact] = {
    val jv = Helpers.tryo {
      parse(str)
    }
    jv match {
      case Full(JObject(obj)) => processJson(obj)
      case x: EmptyBox        => x
      case _                  => Failure("The JSON is not a map, or does not contain the 'type' definition")
    }
  }

  def processJson(obj: JObject): Box[CompilerArtifact] = {
    obj \ "type" match {
        case JString(v) => v.toLowerCase() match {
            case "routes" => Routes.fromJson(obj \ "routes")
            case "definitions" => Definitions.fromJson(obj \ "definitions")
            case str => Failure(s"The type ${str} is unknown")
        }
        case _ => Failure("The 'type' parameter is not a string")
    }
  }
}

