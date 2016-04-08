package com.bigpanda.streaming

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializerSettings, Supervision, ActorMaterializer}
import akka.stream.scaladsl._
import com.bigpanda.data.memory.{MemoryEventsByWordRepository, MemoryEventTypeRepository}
import com.bigpanda.data.redis.{RedisEventsByWordRepository, RedisEventTypeRepository}
import com.bigpanda.data.{EventsByWordRepository, EventTypeRepository, Event}

import org.json4s._
import org.json4s.native.Serialization

import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
  * Created by olgagorun on 08/04/2016.
  *  ~/Downloads/generator-macosx-amd64 | sbt 'run-main com.bigpanda.streaming.EventProcessor'
  */
object EventProcessor extends App {
  implicit val system = ActorSystem("bigpanda-events")
  import system.dispatcher
  private val decider: Supervision.Decider = {
    case _: org.json4s.ParserUtil.ParseException => Supervision.Resume
    case _                      => Supervision.Stop
  }
  implicit val mat = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  val config = system.settings.config
  val redisEventsByWordRepository = new RedisEventsByWordRepository(config.getString("app.redis.host"), config.getInt("app.redis.port"))
  val redisEventTypeRepository = new RedisEventTypeRepository(config.getString("app.redis.host"), config.getInt("app.redis.port"))

  val generatorSource = Source.fromIterator(() => Iterator.continually(io.StdIn.readLine()))
  processStream(generatorSource, redisEventsByWordRepository, redisEventTypeRepository)


  def processStream(source: Source[String, Unit], eventsByWordRepository: EventsByWordRepository, eventsByTypeRepository: EventTypeRepository) = {
    implicit val formats = DefaultFormats

    val concreteIncrementEventType = incrementEventType(eventsByTypeRepository)
    val concreteIncrementEventWord = incrementEventByWord(eventsByWordRepository)
    val deserializeJSON = Flow[String].map(Serialization.read[Event])
    val consoleSink = Sink.foreach[Event](println)

    source.
      via(deserializeJSON).
      via(concreteIncrementEventType).
      via(concreteIncrementEventWord).
      runWith(consoleSink)
  }

//  def testStream() = {
//    val testLines = """{ "event_type": "baz", "data": "amet", "timestamp": 1460077237 }
//                      |�٧F���
//                      |{ "����:Ϡ
//                      |{ "�,���&2{<
//                      |{ "event_type": "foo", "data": "lorem", "timestamp": 1460077243 }
//                      |{ "event_type": "baz", "data": "ipsum", "timestamp": 1460077243 }
//                      |{ "event_type": "foo", "data": "dolor", "timestamp": 1460077243 }
//                      |{ "event_type": "foo", "data": "lorem", "timestamp": 1460077243 }
//                      |{ "8=��{��G
//                      |{ "event_type": "foo", "data": "ipsum", "timestamp": 1460077246 }
//                      |{ "event_type": "bar", "data": "lorem", "timestamp": 1460077246 }
//                      |{ "event_type": "bar", "data": "dolor", "timestamp": 1460077246 }
//                      |{ "event_type": "bar", "data": "sit", "timestamp": 1460077246 }
//                      |{ "6|��Jl�
//                      |{ "event_type": "bar", "data": "lorem", "timestamp": 1460077246 }
//                      |{ "event_type": "bar", "data": "lorem", "timestamp": 1460077246 }
//                      |{ "��M��^��&
//                      |{ "event_type": "baz", "data": "dolor", "timestamp": 1460077246 }
//                      |{ "event_type": "bar", "data": "ipsum", "timestamp": 1460077246 }
//                      |{ "event_type": "bar", "data": "sit", "timestamp": 1460077248 }
//                      |{ "=1d�#p��
//                      |{ "event_type": "bar", "data": "sit", "timestamp": 1460077248 }
//                      |{ "event_type": "baz", "data": "dolor", "timestamp": 1460077248 }
//                      |{ "event_type": "foo", "data": "dolor", "timestamp": 1460077248 }""".stripMargin.split("\\n").toIterator
//
//    val testSource = Source.fromIterator(() => testLines)
//
//    processStream(testSource, MemoryEventsByWordRepository, MemoryEventTypeRepository).onComplete { case _ =>
//      val future = for {
//        eventsByType <- MemoryEventTypeRepository.getAll()
//        eventsByWord <- MemoryEventsByWordRepository.getAll()
//      } yield (eventsByType, eventsByWord)
//      val result = Await.result(future, Duration.create(5, TimeUnit.SECONDS))
//      println(result)
//      system.shutdown()
//    }
//  }

  private def incrementEventType(repository: EventTypeRepository) = Flow[Event].map { event => Await.result(repository.increment(event.event_type), Duration.create(5, TimeUnit.SECONDS)); event }

  private def incrementEventByWord(repository: EventsByWordRepository) = Flow[Event].map { event => Await.result(repository.increment(event.data), Duration.create(5, TimeUnit.SECONDS)); event }
}
