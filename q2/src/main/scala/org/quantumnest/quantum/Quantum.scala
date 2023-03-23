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
import net.liftweb.json.JsonAST
import net.liftweb.common.Failure

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

case class StateDef(name: String)

object Quantum {
    def extractState(obj: JsonAST.JValue): Box[Seq[StateDef]] = {
    obj \ "state" match {
        case JsonAST.JNull => Full(Nil)
        case JsonAST.JObject(v) => Full(v.map(f => StateDef(f.name)))
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