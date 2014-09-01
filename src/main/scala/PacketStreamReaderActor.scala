package com.orbinista.enmeerwerterfasser

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import akka.actor.{Props, ActorLogging, Actor}
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

  val receiver = setupReceiver

  val logger = system.actorOf(Props[CsvValueLoggerActor], name = "csv-logger")

  def receive = {
    case ReadPacket =>
      val receivedPacket: BasicPacket = receiver.read
      if (receivedPacket != null) {
        extractInformation(receivedPacket) foreach (logger ! _)
      }
      self ! ReadPacket
  }

  override def postStop() = {
    connector.disconnect()
    system.shutdown()
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

  def extractInformation(receivedPacket: BasicPacket): ListBuffer[LogData] = {
    val data = new ListBuffer[LogData]
    if (receivedPacket.isInstanceOf[RadioPacket]) {
      val radioPacket: RadioPacket = receivedPacket.asInstanceOf[RadioPacket]
      val profile = profiles.get(radioPacket.getSenderId.toString)
      profile match {
        case Some(profile) => {
          val javaMap = profile.parsePacket(radioPacket)
          javaMap.foreach {
            case (key: EnoceanParameterAddress, number: NumberWithUnit) => {
              number.getValue match {
                case big: BigDecimal =>
                  data += new LogData(senderId = key.getEnoceanDeviceId, value = big.floatValue(), unit = number.getUnit.toString, datetime = DateTime.now)
                case value: Number =>
                  data += new LogData(senderId = key.getEnoceanDeviceId, value = value.floatValue, unit = number.getUnit.toString, datetime = DateTime.now)
              }
            }
            case _ => None
          }
        }
        case _ => None
      }
    }
    data
  }
}