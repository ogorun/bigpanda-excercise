package com.bigpanda.data.memory

import com.bigpanda.data.{EventsByWordRepository, EntityCounterRepository, EventTypeRepository}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by olgagorun on 07/04/2016.
  */


// TODO: For use in any way besides testing, provide thread safe version
sealed class MemoryEntityCounterRepository extends EntityCounterRepository {
  private lazy val storage: mutable.Map[String, Int] = mutable.Map[String, Int]().withDefaultValue(0)

  override def increment(entity: String): Future[Int] = Future {
    val newVal = storage.getOrElse(entity, 0) + 1
    storage.update(entity, newVal)
    newVal
  }

  override def get(entity: String): Future[Int] =
    Future(storage(entity))

  override def getAll(): Future[Map[String, Int]] =
    Future(storage.toMap)

}

object MemoryEventTypeRepository extends MemoryEntityCounterRepository with EventTypeRepository

object MemoryEventsByWordRepository extends MemoryEntityCounterRepository with EventsByWordRepository
