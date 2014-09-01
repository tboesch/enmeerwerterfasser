package com.orbinista.enmeerwerterfasser

import akka.actor.{ActorLogging, Actor}
import com.github.tototoshi.csv._

class CsvValueLoggerActor extends Actor with ActorLogging {

  def receive = {
    case data: LogData => {
      log.info(data.toString)
      val writer = CSVWriter.open("a.csv", append = true)
      //writer.writeRow(data.unapply)
    }
  }

}

