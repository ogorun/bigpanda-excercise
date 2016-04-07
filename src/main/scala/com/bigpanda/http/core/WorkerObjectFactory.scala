package com.bigpanda.http.core

import akka.util.Timeout
import com.bigpanda.data.{EventsByWordRepository, EventTypeRepository}

import scala.concurrent.duration.Duration

/**
  * Created by olgagorun on 08/04/2016.
  */
trait WorkerObjectFactory {
  def eventTypeRepository: EventTypeRepository
  def eventsByWordRepository: EventsByWordRepository
  def timeout: Duration
}
