package com.bigpanda.data.memory

import com.bigpanda.data.{EventsByWordRepository, EntityCounterRepository, EventTypeRepository}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by olgagorun on 07/04/2016.
  */

class MemoryEntityCounterRepository extends EntityCounterRepository {
  private lazy val storage: mutable.Map[String, Int] = mutable.Map[String, Int]().withDefaultValue(0)

  override def increment(entity: String): Future[Unit] =
    Future(storage.update(entity, storage.getOrElse(entity, 0) + 1))

  override def get(entity: String): Future[Int] =
    Future(storage(entity))

  override def getAll(): Future[Map[String, Int]] =
    Future(storage.toMap)

}

object MemoryEvenTypeRepository extends MemoryEntityCounterRepository with EventTypeRepository

object MemoryEventsByWordRepository extends MemoryEntityCounterRepository with EventsByWordRepository
