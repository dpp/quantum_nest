package org.quantumnest.compiler

import net.liftweb.json._
// import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.common.{Box, Full, Empty, EmptyBox}
import net.liftweb.util.Helpers

case class Routes(routes: List[Route]) extends CompilerArtifact 

object Routes {
  implicit val formats =
    Serialization.formats(ShortTypeHints(List(classOf[Routes], classOf[Route])))

  def fromJson(s: String): Box[Routes] = {
    val jval = Helpers.tryo {
      parse(s);
    }

    jval match {
      case Full(v)     => fromJson(v)
      case x: EmptyBox => x
    }
  }

  def fromJson(s: JValue): Box[Routes] = {
    Helpers.tryo {
      s.extract[Routes]
    }
  }

  def toJson(source: Routes): JValue = {
    write(source)
  }
}


case class Route(
    method: String,
    route: List[String],
    queryParams: List[Map[String, List[String]]],
    headers: List[Map[String, List[String]]],
    message: JValue
) extends CompilerArtifact {

  def toJson(): String = {
    compactRender(Route.toJson(this))
  }

}

object Route {
  implicit val formats =
    Serialization.formats(ShortTypeHints(List(classOf[Route])))

  def fromJson(s: String): Box[Route] = {
    val jval = Helpers.tryo {
      parse(s);
    }

    jval match {
      case Full(v)     => fromJson(v)
      case x: EmptyBox => x
    }
  }

  def fromJson(s: JValue): Box[Route] = {
    Helpers.tryo {
      s.extract[Route]
    }
  }

  def toJson(source: Route): JValue = {
    write(source)
  }
}

