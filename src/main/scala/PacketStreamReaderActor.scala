package com.orbinista.enmeerwerterfasser

import scala.collection.JavaConversions._

import akka.actor.{ActorLogging, Actor}
import org.opencean.core.address.{EnoceanId, EnoceanParameterAddress}
import org.opencean.core.common.EEPId
import org.opencean.core.common.values.NumberWithUnit
import org.opencean.core.eep.{EltakoLumSensor, TemperaturSensor, EltakoHumidityAndTemperatureSensor}
import org.opencean.core.packets.{RadioPacket, BasicPacket}
import org.opencean.core.{PacketStreamReader, EnoceanSerialConnector}

import com.github.nscala_time.time.Imports._

case class ReadPacket()

case class LogData(senderId: EnoceanId, value: Float, unit: String, datetime: DateTime)

class PacketStreamReaderActor extends Actor with ActorLogging {

  import context._
  import PacketStreamReaderActor._

  log.info("Setting up PacketStreamReaderActor")

  val receiver = setupReceiver

  override def postStop() = {
    connector.disconnect()
    system.shutdown()
  }

  def receive = {
    case ReadPacket =>
      val receivedPacket: BasicPacket = receiver.read
      if (receivedPacket != null) {
        extractInformation(receivedPacket) match {
          case Some(data: LogData) => {
            log.info(data.toString)
          }
          case None =>
         }
      }
      self ! ReadPacket
  }
}


object PacketStreamReaderActor {
  val profiles = Map(
    ("01:00:5E:A8", new EltakoHumidityAndTemperatureSensor),
    ("01:8B:F3:4A", new EltakoHumidityAndTemperatureSensor),
    ("00:06:C6:81", new TemperaturSensor(10, 90, EEPId.EEP_A5_02_17)),
    ("01:00:72:69", new EltakoLumSensor)
  )

  val connector = new EnoceanSerialConnector()

  def setupReceiver = {
    val port = "/dev/cu.usbserial-FTWYOD2G"
    connector.connect(port)
    new PacketStreamReader(connector)
  }

  def extractInformation(receivedPacket: BasicPacket): Option[LogData] = {
    if (receivedPacket.isInstanceOf[RadioPacket]) {
      val radioPacket: RadioPacket = receivedPacket.asInstanceOf[RadioPacket]
      val profile = profiles.get(radioPacket.getSenderId.toString)
      profile match {
        case Some(profile) => {
          val javaMap = profile.parsePacket(radioPacket)
          javaMap.foreach {
            case (key: EnoceanParameterAddress, number: NumberWithUnit) => {
              number.getValue match {
                case big: BigDecimal => {
                  return Some(new LogData(senderId = key.getEnoceanDeviceId, value = big.floatValue(), unit = number.getUnit.toString, datetime = DateTime.now))
                }
                case value: Number => {
                  return Some(new LogData(senderId = key.getEnoceanDeviceId, value = value.floatValue, unit = number.getUnit.toString, datetime = DateTime.now))
                }
              }
            }
            case _ => None
          }
        }
        case _ => None
      }
    }
    None
  }
}