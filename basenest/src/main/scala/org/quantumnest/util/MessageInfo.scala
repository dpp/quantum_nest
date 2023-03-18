package org.quantumnest.util

/** The value of a node in a message
  */
sealed trait MessageValue {}

object MessageValue {
  type MessageShape = java.util.Map[Symbol, MessageValue]
}

final case class StringValue(value: String) extends MessageValue
final case class ObjectValue(value: MessageValue.MessageShape)
    extends MessageValue
