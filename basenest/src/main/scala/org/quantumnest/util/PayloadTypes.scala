package org.quantumnest.util

/**
  * Envelopes have a payload type... what's the type of the shape of data?
  * Here's an enumeration
  */
sealed trait PayloadType

final case object HttpRequestPayload extends PayloadType
final case object HttpResponsePayload extends PayloadType
final case class ExtendedPayloadType(theType: String) extends PayloadType
