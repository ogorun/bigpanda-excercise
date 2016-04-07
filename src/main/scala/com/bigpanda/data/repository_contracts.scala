package com.bigpanda.data

import scala.concurrent.Future

/**
  * Created by olgagorun on 07/04/2016.
  */
trait EntityCounterRepository {
  def increment(entity: String) : Future[Unit]
  def get(entity: String) : Future[Int]
  def getAll(): Future[Map[String, Int]]
}

trait EventTypeRepository extends EntityCounterRepository

trait EventsByWordRepository extends EntityCounterRepository