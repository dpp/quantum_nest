package org.quantumnest.quantum
import scala.reflect.runtime.universe._
import java.util.concurrent.atomic.AtomicReference
import net.liftweb.common.{Box, Full, Empty, Failure}
import org.quantumnest.clojure.ExecFunction

/**
  * State Information 
  *
  * @param owner
  * @param name
  * @param predicates
  * @param dependents
  * @param manifest
  */
case class State[T](
    owner: Quantum,
    name: String,
    predicates: Vector[PredicateSource],
    dependents: Vector[PredicateSource],
    toExec: Box[ExecFunction]
)(implicit val manifest: TypeTag[T])
    extends ReadonlyState[T] {
  val data = new AtomicReference[Box[T]]()
  def value: Box[T] = data.get()
  def resolved_? : Boolean = data.get().isDefined

  def resolve(v: T): Unit = {
    data.get() match {
        case Full(_) => throw new Exception(f"Cannot resolve state ${name} more than once")
        case Empty => data.set(Full(v))
        case f: Failure => throw new Exception(f"Trying to set the value of state ${name} which has already failed ${f}")
    }

  }
}


