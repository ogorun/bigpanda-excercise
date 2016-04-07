package com.bigpanda.http.core

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

object WorkerActor {
  case class Ok(aggregation: Aggregation)
  case object Aggregations
}

class WorkerActor(objectFactory: WorkerObjectFactory) extends Actor with ActorLogging {
  import WorkerActor._

  def receive = {
    case Aggregations =>
      log.info(s"Aggregations request got")
      sender ! Ok(aggregation)
  }

  private def aggregation: Aggregation = {
    val eventTypesFuture = objectFactory.eventTypeRepository.getAll()
    val eventsByWordFuture = objectFactory.eventsByWordRepository.getAll()
    val future = for {
      eventTypes <- eventTypesFuture
      eventsByWord <- eventsByWordFuture
    } yield Aggregation(eventTypes, eventsByWord)
    Await.result(future, objectFactory.timeout)
  }
}

