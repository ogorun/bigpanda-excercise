package com.bigpanda.data.redis

import com.bigpanda.data.{EventTypeRepository, EventsByWordRepository, EntityCounterRepository}
import com.redis.RedisClient
import com.redis.serialization.Parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Parse.Implicits._

/**
  * Created by olgagorun on 08/04/2016.
  */


// TODO: handle connection close and errors
sealed class RedisEntityCounterRepository(host: String, port: Int, key: String) extends EntityCounterRepository {
  private lazy val client = new RedisClient(host, port)

  override def increment(entity: String): Future[Int] =
    Future(client.hincrby(key, entity, 1).getOrElse(1L).toInt)

  override def get(entity: String): Future[Int] =
    Future(client.hget[Int](key, entity).getOrElse(0))

  override def getAll(): Future[Map[String, Int]] =
    Future(client.hgetall[String,Int](key).getOrElse(Map()))
}

class RedisEventsByWordRepository(host: String, port: Int, key: String = "events_by_word") extends RedisEntityCounterRepository(host, port, key) with EventsByWordRepository
class RedisEventTypeRepository(host: String, port: Int, key: String = "events_by_type") extends RedisEntityCounterRepository(host, port, key) with EventTypeRepository