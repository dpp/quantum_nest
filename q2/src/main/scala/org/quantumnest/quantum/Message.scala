package org.quantumnest.quantum

import java.util.concurrent.atomic.AtomicReference
import org.quantumnest.util.Envelope
import net.liftweb.common.{Empty, Box, Full, Failure}

trait Message {
    def owner: Quantum
    def name: String
}
case class ReceivedMessage( owner: Quantum,
    name: String,
    dependents: Vector[PredicateSource],
    ) extends Message {
        val received = new AtomicReference[Box[Envelope]](Empty)
    }

    case class ComposingMessage( owner: Quantum,
    name: String,
    predicates: Vector[PredicateSource],
    ) extends Message {
        
    }
