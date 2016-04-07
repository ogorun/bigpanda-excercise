package com.bigpanda.http.core

import java.util.concurrent.TimeUnit

import com.bigpanda.data.memory.{MemoryEventTypeRepository, MemoryEventsByWordRepository}
import com.bigpanda.data.redis.{RedisEventsByWordRepository, RedisEventTypeRepository}
import com.bigpanda.data.{EventTypeRepository, EventsByWordRepository}
import com.typesafe.config.Config

import scala.concurrent.duration.Duration

/**
  * Created by olgagorun on 08/04/2016.
  */
class DefaultWorkerObjectFactory(config: Config) extends WorkerObjectFactory {
  private lazy val duration = Duration.create(config.getInt("app.workerTimeout"), TimeUnit.SECONDS)

  override def eventTypeRepository: EventTypeRepository =
    new RedisEventTypeRepository(config.getString("app.redis.host"), config.getInt("app.redis.port"), config.getString("app.redis.events_by_type_key")) // MemoryEventTypeRepository

  override def eventsByWordRepository: EventsByWordRepository =
    new RedisEventsByWordRepository(config.getString("app.redis.host"), config.getInt("app.redis.port"), config.getString("app.redis.events_by_word_key")) //MemoryEventsByWordRepository

  override def timeout: Duration =
    duration
}
