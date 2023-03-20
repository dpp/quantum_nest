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

/** The envelope around a message
  */
trait Envelope {

  /** Get the response channel if one is associated with this envelope
    *
    * @return
    *   the optional return channel for the message contained in this envelope
    */
  def getResponseChannel(): Box[Channel[Envelope]]

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
  def withNewChannel(channel: Box[Channel[Envelope]]): Envelope

  /** Create a new Envelope with a new payload
    *
    * @param payload the payload
    * @return a new `Envelope` instance
    */
  def withNewPayload(payload: MessageValue.MessageShape): Envelope
}

object Envelope {
  def apply(
      responseChannel: Box[Channel[Envelope]],
      payload: MessageValue.MessageShape,
      mimeType: PayloadType
  ): Envelope = new SimpleEnvelope(responseChannel, payload, mimeType)
}

class SimpleEnvelope(
    responseChannel: Box[Channel[Envelope]],
    payload: MessageValue.MessageShape,
    mimeType: PayloadType
) extends Envelope {

  /** Get the response channel if one is associated with this envelope
    *
    * @return
    *   the optional return channel for the message contained in this envelope
    */
  def getResponseChannel(): Box[Channel[Envelope]] = responseChannel

  /** Get the mime type of the message payload. The payload is a series of
    * symbol/value pairs.
    *
    * @return
    *   the payload type
    */
  def getMimeType(): PayloadType = mimeType

  /** Get the envolope's message payload
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
  def withNewChannel(channel: Box[Channel[Envelope]]): Envelope =
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
  Channel.addChannel(this)
 
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

trait Channel[T] {
  def send(v: T): Boolean

  def receive(): Box[T]

  def receive(timeout: Long): Box[T]

  def getUUID(): String

  def getURL(): String

  def asA[A](implicit m: TypeTag[A]): Box[Channel[A]]
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
