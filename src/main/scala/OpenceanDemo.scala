package com.orbinista.enmeerwerterfasser

import akka.actor.{Props, ActorSystem}

object OpenceanDemo extends App {
  override def main(args: Array[String]) {

    val system = ActorSystem("flow")

    val reader = system.actorOf(Props[PacketStreamReaderActor], name = "usb-300")

    reader ! ReadPacket

    system.registerOnTermination(println("Stopped terminal system."))
  }

}
