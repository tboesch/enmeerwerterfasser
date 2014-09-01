package com.orbinista.enmeerwerterfasser

import akka.actor.{ActorLogging, Actor}
import com.github.tototoshi.csv._

class CsvValueLoggerActor extends Actor with ActorLogging {

  def receive = {
    case data: LogData => {
      log.info(data.toString)
      val writer = CSVWriter.open("enocean_data_log.csv", append = true)
      writer.writeRow(List(data.datetime.toString("YYYY-MM-dd HH:mm:ss"), data.senderId.toString, data.value.toString, data.unit))
    }
  }

}

