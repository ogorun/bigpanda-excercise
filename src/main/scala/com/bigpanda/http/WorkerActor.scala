package com.bigpanda.http

import akka.actor.{Actor, ActorLogging}

object WorkerActor {
  case class Ok(id: Int)
  case class Create(foo: Foo)
  case object Aggregations
}

class WorkerActor extends Actor with ActorLogging {
  import WorkerActor._

  def receive = {
    case Aggregations =>
      log.info(s"Aggregations")
      sender ! Ok(util.Random.nextInt(10000))
  }
}

