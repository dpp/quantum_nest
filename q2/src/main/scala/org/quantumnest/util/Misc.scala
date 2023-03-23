package org.quantumnest.util

import java.util.UUID

/**
  * Misc helpful stuff
  */
object Misc {
    /**
      * Generate a random UUID-based namespace
      *
      * @return a valid namespace that was derived from a UUID
      */
  def randomUUIDBasedNamespace(): String = {
    f"NS${UUID.randomUUID().toString().replace('-', '_')}"
  }
}
