package org.quantumnest.quantum

import java.util.concurrent.atomic.AtomicBoolean

/**
  * The definition a set of message handling and state.
  * 
  * 
  *
  */
class QuantumDefinition(val name: String) {
    private val valid = new AtomicBoolean(true)
    def cleanup() {

    }
}
