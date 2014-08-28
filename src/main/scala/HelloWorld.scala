package com.orbinista.enmeerwerterfasser

import org.opencean.core.common.ProtocolConnector;
import org.opencean.core.packets.BasicPacket;
import org.opencean.core.packets.QueryIdCommand
import org.opencean.core.{ESP3Host, EnoceanSerialConnector}
;

object HelloWorld {
  def main(args: Array[String]) {
    val port = "/dev/cu.usbserial-FTWYOD2G";
    val connector = new EnoceanSerialConnector();
    connector.connect(port);
    val esp3Host = new ESP3Host(connector);
    val packet = new QueryIdCommand();
    esp3Host.sendRadio(packet);
    esp3Host.start();
  }
}
