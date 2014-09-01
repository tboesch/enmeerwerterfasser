package com.orbinista.enmeerwerterfasser

import akka.actor.{ActorLogging, Actor}

class CsvValueLoggerActor extends Actor with ActorLogging {

  def receive = {
    case data: LogData => {
      log.info(data.toString)
    }
  }

}

