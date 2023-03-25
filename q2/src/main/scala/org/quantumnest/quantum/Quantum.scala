package org.quantumnest.quantum

import java.util.concurrent.atomic.AtomicBoolean
import net.liftweb.common.Box
import org.quantumnest.util.{SendChannel, Envelope, Channel, Misc}
import java.util.concurrent.atomic.AtomicReference
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.EmptyBox
import scala.reflect.runtime.universe._
import java.util.UUID
import net.liftweb.json.{JsonAST}
import JsonAST._
import net.liftweb.common.Failure
import clojure.lang.IFn
import org.quantumnest.clojure.Util
import clojure.lang.ISeq
import net.liftweb.util.Helpers

/**
  * An executing Quantum
  *
  */
trait Quantum {
    /**
      * Gets the main channel to send messages to this Quantum.
      * If the `Channel` is closed or the Quantum is finished,
      * the `Box` will be `Empty`
      *
      * @return the main `Channel` to send messages to
      */
   def mainChannel: Box[SendChannel[Envelope]]

   /**
     * Deliver a message to the Quantum's main channel
     *
     * @param msg the message to deliver
     * @return true if the message was queued
     */
   def sendMessage(msg: Envelope): Boolean = {
    this.mainChannel match {
        case Full(ch) => ch.send(msg)
case _ => false
    }
   }

   /**
     * Is the Quantum still executing messages or
     * otherwise waiting for input
     *
     * @return
     */
   def running_? : Boolean 

   /**
     * For a given state name, get the `ReadonlyState`
     *
     * @param name
     * @return
     */
   def getUntypedState(name: String): Box[ReadonlyState[_]]

   /**
     * Get the typed state
     *
     * @param name the name of the state to get
     * @param manifest the manifest
     * @return a `Full` `Box` if the named state exists and is the correct type
     */
   def getState[T](name: String)(implicit manifest: TypeTag[T]): Box[ReadonlyState[T]]

   /**
     * Get the UUID of the Quantum instance
     *
     * @return
     */
   def uuid : String 

   /**
     * Get the definition of the Quantum instance
     *
     * @return the defintion
     */
   def definition: QuantumDefinition
}

/**
  * The definition of 
  */
trait QuantumDefinition {
    def uuid : String

}

/**
  * 
  */
sealed trait PredicateSource {
  def varName: Box[String]

  def updateVarName(newVarName: String): PredicateSource
}

object PredicateSource {
    def fromJObject(obj: JsonAST.JField): Vector[Box[PredicateSource]] = {
        obj match {
            case JsonAST.JField("from_message", info) => predicateInfo(info).map{ case Full((name, varName)) => Full(FromMessageSource(name, varName)); case Empty => Empty; case f: Failure => f}
            case JsonAST.JField("from_state", info) => predicateInfo(info).map{ case Full((name, varName)) => Full(FromStateSource(name, varName)); case Empty => Empty; case f: Failure => f}
            case _ => Vector()
        }
    }

    def predicateInfo(obj: JValue): Vector[Box[(String, Box[String])]] = {
      obj match {
        case JString(v) => Vector(Full((v, Empty)))
        case JsonAST.JArray(lst) => lst.toVector.flatMap(predicateInfo(_)) 
        case jo: JObject => {
          (jo \ "from", jo \ "var") match {
            case (JString(from), JString(variable)) => Vector(Full((from, Full(variable.trim()))))
            case (JString(from), bad) => Vector(Full((from, Failure(f"Could not convert ${bad} into a variable name"))))
            case bad => Vector(Failure(f"Could not get `from` and `var` from ${bad}"))
          }
        }
        case other => Vector(Failure(f"Could not get predicate `from` and `var` from ${other}"))
      }
    }
}

case class ExecFunction(func: IFn, variables: Vector[String]) {
  def exec(in: Map[String, Any]): Box[Object] = {

    val toPass: Vector[Any] = variables.map(v => in.get(v).getOrElse(null))
Helpers.tryo(    toPass match {
  case Vector() => func.invoke()
  case Vector(a) =>  func.invoke(a)
  case Vector(a,b) => func.invoke(a,b)
  case Vector(a,b, c) => func.invoke(a,b, c)
  case Vector(a,b, c, d) => func.invoke(a,b, c, d)
  case Vector(a,b, c, d, e) => func.invoke(a,b, c, d, e)
  case Vector(a,b, c, d, e, f) => func.invoke(a,b, c, d, e, f)
  case Vector(a,b, c, d, e, f, g) => func.invoke(a,b, c, d, e, f, g)
  case Vector(a,b, c, d, e, f, g, h) => func.invoke(a,b, c, d, e, f, g, h)
  case _ => func.applyTo(Util.toClojureVector(toPass).seq) 
})
  }
}

object ExecFunction {
  def fromJObject(namespace: String, obj: JsonAST.JField, variables: Vector[String]): Box[ExecFunction] = {
    if (obj.name == "exec") {
      obj.value match {
        case JsonAST.JString(toExec) => {
          val funcName = Misc.randomUUIDBasedNamespace()
          val code = Util.compileCode(namespace, List((funcName, variables, toExec)))
          val theFn = code.flatMap(v => v.get(funcName))

          theFn match {
            case Empty => Empty
            case v: Failure => v 
            case Full(compiledFn) => Full(ExecFunction(compiledFn, variables))
          }
        }
        case v => Failure(f"Could not create an execution block with ${v}")
      }
    } else
    Empty
  }
}

final case class FromMessageSource(message: String, varName: Box[String]) extends PredicateSource {
  def updateVarName(newVarName: String): PredicateSource = FromMessageSource(message, Full(newVarName))
}
final case class FromStateSource(state: String, varName: Box[String]) extends PredicateSource {
  def updateVarName(newVarName: String): PredicateSource = FromStateSource(state, Full(newVarName))
}

case class StateDef(name: String, predicates: Seq[PredicateSource], toExec: Box[ExecFunction]) 

object StateDef {
    def from(namespace: String, definition: JsonAST.JField): Box[StateDef] = {
        val name = definition.name
        definition.value match {
            case JsonAST.JObject(fields) => {
                val predicatesBase = fields.flatMap(f => PredicateSource.fromJObject(f)).toVector

                val failedPredicates = predicatesBase.flatMap{ case f: Failure => List(f); case _ => Nil }
                val predicatesPre = predicatesBase.flatMap{v => v}

                val (predicates, varNames) = {
                  var cnt = 0
                  val p2 = predicatesPre.map{
                    // set a name for all un-named variables
                    case p if p.varName.isEmpty => {
                      val newName = if (cnt == 0) "it" else f"it_${cnt}"
                      cnt = cnt + 1
                      p.updateVarName(newName)
                    }
                    case p => p
                  }

                  (p2, p2.flatMap(_.varName))
                }
                // build the execs
                val execStuff = fields.map(f => ExecFunction.fromJObject(namespace, f, varNames)).toVector

                val execFailures = execStuff.flatMap{
                  case v: Failure => Full(v)
                  case _ => Empty
                }.appendedAll(failedPredicates)

                val execs = execStuff.flatMap(v => v)

                if (predicates.length == 0) {
                  Failure(f"No predicates defined for ${name}")
                }
                else if (execFailures.length > 0) {

                  val returnList = Failure(f"Unable to compile State information for ${name}") :: execFailures.toList

                  def chainFailures(ff: List[Failure]): Failure = {
                    ff match {
                      case Nil => Failure("Empty chain list")
                      case v :: Nil => v
                      case v :: rest => Failure(v.msg, v.exception, Full(chainFailures(rest)))
                    }
                  }

                  chainFailures(returnList)
                } else if (execs.length > 1) {
                  Failure(f"Only one execution block allowed for State ${name}")
                } else if (execs.length == 0 && predicates.length > 1) {
                  Failure(f"For State ${name}, there are ${predicates.length} but no function to convert them into a value")
                } else {
                  Full(StateDef(name, predicates, execs.headOption))
                }
            }
            case v => Failure(f"Could not create a State definition for ${name} because value isn't an Object, but `${v}`")
        }
        
    }
}

object Quantum {
    def extractState(namespace: String, obj: JsonAST.JValue): Box[Seq[Box[StateDef]]] = {
    obj \ "state" match {
        case JsonAST.JNull => Full(Nil)
        case JsonAST.JObject(v) => Full(v.map(f => StateDef.from(namespace, f)))
        case v => Failure(f"The 'state' definition is not an Object, it's ${v}")
    }
  }
}
    


class SimpleQuantum(val definition: QuantumDefinition, val name: String) extends Quantum {

    val states = new AtomicReference[Map[String, State[_]]]()
    val running = new AtomicBoolean(true)
    val channel = Channel[Envelope]()
    val theUuid = Misc.randomUUIDBasedNamespace()

    def SimpleQuantum(defined: QuantumDefinition, n: String) {
        SimpleQuantum(defined, n)

    }

    Thread.ofVirtual().name(f"Quantum ${name}").start(() => {
        while (running.get()) {
            // get a message or timeout after 100ms
            channel.receive(100) match {
                case Full(msg) => 42 // FIXME do something with the incoming message
                case _ => 0
            }

        }
    })

    class State[T](val name: String, val predicates: Vector[Predicate], val dependants: Vector[Predicate])(implicit val manifest: TypeTag[T]) extends ReadonlyState[T] {
        val data = new AtomicReference[Box[T]]()
        def value: Box[T] = data.get()
        def resolved_? : Boolean = data.get().isDefined
        def owner: Quantum = SimpleQuantum.this
    }

       /**
     * For a given state name, get the `ReadonlyState`
     *
     * @param name
     * @return
     */
   def getUntypedState(name: String): Box[ReadonlyState[_]] = 
    states.get().get(name)

       /**
     * Get the typed state
     *
     * @param name the name of the state to get
     * @param manifest the manifest
     * @return a `Full` `Box` if the named state exists and is the correct type
     */
   def getState[T](name: String)(implicit manifest: TypeTag[T]): Box[ReadonlyState[T]] = {
    states.get().get(name) match {
        case Some(state) if state.manifest == manifest => Full(state).asInstanceOf[Box[ReadonlyState[T]]] 
        case _ => Empty
    }

   }

   def uuid : String = this.theUuid

    def running_? : Boolean = running.get()

    def mainChannel: Box[SendChannel[Envelope]] = Full(channel)
}

/**
  * State that can be shared outside a Quantum
  */
trait ReadonlyState[T] {
    /**
      * The value of the state. `Empty` if the predicates of the state
      * have not yet been resolved
      *
      * @return a `Box` containing the value of the state
      */
    def value: Box[T]

    /**
      * Has the State been resolved (all its predicates have
      * been satisfied and the value has been computed)
      *
      * @return true if the value was resolved
      */
    def resolved_? : Boolean

    /**
      * The name of the state
      *
      * @return
      */
    def name: String

    /**
      * What Quantum owns this State?
      *
      * @return a references to the owning Quantum
      */
    def owner: Quantum

    /**
      * Get the list of predicates for this `State`
      *
      * @return
      */
    def predicates: Seq[Predicate]
}

/**
  * A Predicate for a particular State
  */
sealed trait Predicate

final case class StatePredicate(name: String) extends Predicate
final case class MessagePredicate(name: String) extends Predicate