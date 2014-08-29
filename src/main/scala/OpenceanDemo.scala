package com.orbinista.enmeerwerterfasser

import org.opencean.core.{PacketStreamReader, EnoceanSerialConnector}
import org.opencean.core.packets.{RadioPacket4BS, RadioPacket, BasicPacket}
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import org.opencean.core.eep.{EltakoLumSensor, TemperaturSensor, EltakoHumidityAndTemperatureSensor}
import org.opencean.core.common.{Parameter, EEPId}
import org.opencean.core.address.{EnoceanParameterAddress, EnoceanId}

import scala.collection.JavaConversions._

case class ReadPacket()

case class SendPacket()

class PacketStreamReaderActor extends Actor with ActorLogging {

  import context._

  val profiles = Map(
    ("01:00:5E:A8", new EltakoHumidityAndTemperatureSensor),
    ("01:8B:F3:4A", new EltakoHumidityAndTemperatureSensor),
    ("00:06:C6:81", new TemperaturSensor(10, 90, EEPId.EEP_A5_02_17)),
    ("01:00:72:69", new EltakoLumSensor)
  )

  log.info("Setting up PacketStreamReaderActor")

  val port = "/dev/cu.usbserial-FTWYOD2G"
  val connector = new EnoceanSerialConnector();
  connector.connect(port);
  val receiver: PacketStreamReader = new PacketStreamReader(connector)

  override def postStop() = {
    connector.disconnect()
    system.shutdown()
  }

  def receive = {
    case ReadPacket =>
      val receivedPacket: BasicPacket = receiver.read
      if (receivedPacket != null) {
        if (receivedPacket.isInstanceOf[RadioPacket]) {
          val radioPacket: RadioPacket = receivedPacket.asInstanceOf[RadioPacket]
          val profile = profiles.get(radioPacket.getSenderId.toString)
          profile match {
            case Some(profile) => {
              val javaMap = profile.parsePacket(radioPacket)
              val result = javaMap.mapValues(_.getValue)
              result.foreach {
                case (key: EnoceanParameterAddress, value) => {
                  log.info(s"sensor: ${key.getEnoceanDeviceId.toString}, parameter: ${key.getParameterId}, value: ${value}")
                }
                case _ => None
              }
            }
            case _ => None
          }
        }
      }
      self ! ReadPacket
  }

}


object OpenceanDemo {
  private val logger: Logger = LoggerFactory.getLogger("HelloWorld::Main")

  def main(args: Array[String]) {

    val system = ActorSystem("flow")

    val reader = system.actorOf(Props[PacketStreamReaderActor], name = "usb-300")

    reader ! ReadPacket

    system.registerOnTermination(println("Stopped terminal system."))
  }

}
