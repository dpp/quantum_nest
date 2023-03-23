package org.quantumnest.util

import java.util.concurrent.LinkedTransferQueue
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.util.Helpers
import java.util.concurrent.TimeUnit
import scala.annotation.switch
import net.liftweb.common.Empty
import java.util.WeakHashMap
import java.lang.ref.WeakReference
import java.util.UUID
import scala.reflect.runtime.universe._
import java.util.concurrent.atomic.AtomicBoolean

/** The envelope around a message
  */
trait Envelope {

  /** Get the response channel if one is associated with this envelope
    *
    * @return
    *   the optional return channel for the message contained in this envelope
    */
  def getResponseChannel(): Box[SendChannel[Envelope]]

  /** Get the mime type of the message payload. The payload is a series of
    * symbol/value pairs.
    *
    * @return
    *   the payload type
    */
  def getMimeType(): PayloadType

  /** Get the envolope's message payload
    *
    * @return
    *   the message payload
    */
  def getPayload(): MessageValue.MessageShape

  /** Create a new Envelope with a new channel
    *
    * @param channel
    *   \- the channel for the new envelope
    * @return
    *   the updated envelope
    */
  def withNewChannel(channel: Box[SendChannel[Envelope]]): Envelope

  /** Create a new Envelope with a new payload
    *
    * @param payload the payload
    * @return a new `Envelope` instance
    */
  def withNewPayload(payload: MessageValue.MessageShape): Envelope
}

object Envelope {
  def apply(
      responseChannel: Box[SendChannel[Envelope]],
      payload: MessageValue.MessageShape,
      mimeType: PayloadType
  ): Envelope = new SimpleEnvelope(responseChannel, payload, mimeType)
}

class SimpleEnvelope(
    responseChannel: Box[SendChannel[Envelope]],
    payload: MessageValue.MessageShape,
    mimeType: PayloadType
) extends Envelope {

  /** Get the response channel if one is associated with this envelope
    *
    * @return
    *   the optional return channel for the message contained in this envelope
    */
  def getResponseChannel(): Box[SendChannel[Envelope]] = responseChannel

  /** Get the mime type of the message payload. The payload is a series of
    * symbol/value pairs.
    *
    * @return
    *   the payload type
    */
  def getMimeType(): PayloadType = mimeType

  /** Get the envelope's message payload
    *
    * @return
    *   the message payload
    */
  def getPayload(): MessageValue.MessageShape = payload

  /** Create a new Envelope with a new channel
    *
    * @param channel
    *   \- the channel for the new envelope
    * @return
    *   the updated envelope
    */
  def withNewChannel(channel: Box[SendChannel[Envelope]]): Envelope =
    new SimpleEnvelope(channel, payload, mimeType)

  /** Create a new Envelope with
    *
    * @param payload
    * @return
    */
  def withNewPayload(payload: MessageValue.MessageShape): Envelope =
    new SimpleEnvelope(responseChannel, payload, mimeType)
}

/** A simple multi-producer, multi-consumer channel.
  *
  * It's really a thin wrapper in `LinkedTransferQueue`
  */
class SimpleChannel[T](implicit manifest: TypeTag[T]) extends Channel[T] {
  private lazy val queue = new LinkedTransferQueue[T]()
  private val uuid = UUID.randomUUID()
    private val closed = new AtomicBoolean(false)
  Channel.addChannel(this)
 
  def send(v: T): Boolean = {
    if (closed.get()) 
      false
    else     queue.add(v)
  }


  /**
    * Receive a message or timeout trying
    *
    * @param timeout the timeout duration in milliseconds
    * @return
    */
  def receive(timeout: Long): Box[T] = {
    if (closed.get()) {
      Empty
    } else {
    val ret = Helpers.tryo(queue.poll(timeout, TimeUnit.MILLISECONDS))
    ret match {
      case Full(null) => Empty
      case x          => x
    }
  }
  }

  def getUUID(): String = {
    this.uuid.toString()
  }

  def getURL(): String = {
    f"localchannel:${this.getUUID()}"
  }

  def asA[A](implicit m: TypeTag[A]): Box[Channel[A]] = {
    if (this.manifest == m) Full(this).asInstanceOf[Box[Channel[A]]]
    else Empty
  }

def closed_? : Boolean = closed.get()

def close() {closed.set(true)}

  /**
    * Each channel has a unique UUID. Comparing two channels is
    * the comparison of the UUID and nothing else
    *
    * @param x the other thing to compare
    * @return true if the thing is a Channel and it has the same UUID as this channel
    */
  override def equals(x: Any): Boolean = x match {
    case ch: Channel[_] => ch.getUUID() == this.getUUID()
    case _ => false
  }
}

/**
  * A Channel that can be sent to.
  */
trait SendChannel[T] {
  /**
    * Send a message to the channel
    *
    * @param v
    * @return
    */
  def send(v: T): Boolean

  /**
    * Get a UUID for this channel. The UUID can be used within
    * the same Classloader to look up a channel
    *
    * @return the UUID of the channel
    */
  def getUUID(): String

  /**
    * Get the URL of the Channel. The URL can be used anywhere
    * (across a cluster) to locate a channel, even if the locus of
    * the channel has migrated (e.g. a node goes down, the Quantum processing
    * the channel has been brought up on another node)
    *
    * @return the URL of the channel
    */
  def getURL(): String

  /**
    * Is the channel closed? This value may not be
    * accurate as a remote channel may be closed, but
    * a `SendChannel` proxy may not be updated.
    * 
    * @return true of the channel is no longer processing messages 
    */
    def closed_? : Boolean
}

/**
  * A channel that can only receive. This is a pair to `SendChannel`
  */
trait ReceiveChannel[T] {

  /**
    * Receive a message or timeout trying. If the `Channel` is
    * closed, an `Empty` will be returned
    *
    * @param timeout the timeout duration in milliseconds
    * @return
    */
  def receive(timeout: Long): Box[T]

  /**
    * Get a UUID for this channel. The UUID can be used within
    * the same Classloader to look up a channel
    *
    * @return the UUID of the channel
    */
  def getUUID(): String

    /**
    * Get the URL of the Channel. The URL can be used anywhere
    * (across a cluster) to locate a channel, even if the locus of
    * the channel has migrated (e.g. a node goes down, the Quantum processing
    * the channel has been brought up on another node)
    *
    * @return the URL of the channel
    */
  def getURL(): String

  /**
    * Test the `Channel` to see if it's closed
    *
    * @return
    */
    def closed_? : Boolean
}

trait Channel[T] extends SendChannel[T] with ReceiveChannel[T] {

  def asA[A](implicit m: TypeTag[A]): Box[Channel[A]]

  def close()

  def closed_? : Boolean



}

object Channel {
  private var localMap: WeakHashMap[String, WeakReference[Channel[_]]] =
    new WeakHashMap()

  def apply[T]()(implicit manifest: TypeTag[T]): Channel[T] = new SimpleChannel[T]

  def addChannel(channel: Channel[_]) {
    localMap.synchronized {
      localMap.put(channel.getUUID(), new WeakReference(channel))
    }
  }

  def locateChannel[T: TypeTag](uuid: String): Box[Channel[T]] = {
    localMap.synchronized {
      localMap.get(uuid) match {
        case null => Empty
        case wr => {
          wr.get() match {
            case null => Empty
            case ch   => ch.asA[T]
          }
        }
      }

    }
  }
}
