package org.quantumnest.util

import java.util.concurrent.LinkedTransferQueue
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.util.Helpers
import java.util.concurrent.TimeUnit
import scala.annotation.switch
import net.liftweb.common.Empty

object Util {
    type MessageShape = java.util.Map[Symbol, String]
}

/** A simple multi-producer, multi-consumer channel.
  *
  * It's really a thin wrapper in `LinkedTransferQueue`
  */
class SimpleChannel[T] extends Channel[T] {
  lazy val queue = new LinkedTransferQueue[T]()

  def send(v: T): Boolean = {
    queue.add(v)
  }

  def receive(): Box[T] = {
    Helpers.tryo(queue.take())
  }

  def receive(timeout: Long): Box[T] = {
    val ret = Helpers.tryo(queue.poll(timeout, TimeUnit.MILLISECONDS))
    ret match {
      case Full(null) => Empty
      case x          => x
    }
  }
}

trait Channel[T] {
  def send(v: T): Boolean

  def receive(): Box[T]

  def receive(timeout: Long): Box[T]
}

