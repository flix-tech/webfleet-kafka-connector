package com.flixtech

package object metrics {
  //dimensions
  val httpPoll = List(DimensionItem("Http", "Poll"))
  val httpAck = List(DimensionItem("Http", "Ack"))
  val ioMessage = List(DimensionItem("IO", "Message"))
}
