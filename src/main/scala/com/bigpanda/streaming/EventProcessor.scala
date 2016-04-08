package com.bigpanda.streaming

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.{ClosedShape, ActorMaterializerSettings, Supervision, ActorMaterializer}
import akka.stream.scaladsl._
import com.bigpanda.data.memory.{MemoryEventsByWordRepository, MemoryEventTypeRepository}
import com.bigpanda.data.redis.{RedisEventsByWordRepository, RedisEventTypeRepository}
import com.bigpanda.data.{EventsByWordRepository, EventTypeRepository, Event}

import org.json4s._
import org.json4s.native.Serialization

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.forkjoin.ThreadLocalRandom

/**
  * Created by olgagorun on 08/04/2016.
  */
object EventProcessor extends App {
  implicit val system = ActorSystem("bigpanda-events")
  import system.dispatcher
  val decider: Supervision.Decider = exc => exc match {
    case _: org.json4s.ParserUtil.ParseException => Supervision.Resume
    case _                      => Supervision.Stop
  }
  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))
//  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats
  val str = """{"event_type":"baz","data":"amet","timestamp":1460077237}"""
  //println(Serialization.read[Event](str))

  val testLines = """{ "event_type": "baz", "data": "amet", "timestamp": 1460077237 }
      |�٧F���
      |{ "����:Ϡ
      |{ "�,���&2{<
      |{ "event_type": "foo", "data": "lorem", "timestamp": 1460077243 }
      |{ "event_type": "baz", "data": "ipsum", "timestamp": 1460077243 }
      |{ "event_type": "foo", "data": "dolor", "timestamp": 1460077243 }
      |{ "event_type": "foo", "data": "lorem", "timestamp": 1460077243 }
      |{ "8=��{��G
      |{ "event_type": "foo", "data": "ipsum", "timestamp": 1460077246 }
      |{ "event_type": "bar", "data": "lorem", "timestamp": 1460077246 }
      |{ "event_type": "bar", "data": "dolor", "timestamp": 1460077246 }
      |{ "event_type": "bar", "data": "sit", "timestamp": 1460077246 }
      |{ "6|��Jl�
      |{ "event_type": "bar", "data": "lorem", "timestamp": 1460077246 }
      |{ "event_type": "bar", "data": "lorem", "timestamp": 1460077246 }
      |{ "��M��^��&
      |{ "event_type": "baz", "data": "dolor", "timestamp": 1460077246 }
      |{ "event_type": "bar", "data": "ipsum", "timestamp": 1460077246 }
      |{ "event_type": "bar", "data": "sit", "timestamp": 1460077248 }
      |{ "=1d�#p��
      |{ "event_type": "bar", "data": "sit", "timestamp": 1460077248 }
      |{ "event_type": "baz", "data": "dolor", "timestamp": 1460077248 }
      |{ "event_type": "foo", "data": "dolor", "timestamp": 1460077248 }""".stripMargin.split("\\n").toIterator

  val testSource = Source.fromIterator(() => testLines)

  val consoleSink = Sink.foreach[Event](println)
  val consoleTraceFlow = Flow[Event].map(println)

  val deserializeJSON = Flow[String].map(Serialization.read[Event])

  val addWordsSet = Flow[Event].map {event => event.data.split("\\n")}

  def incrementEventType(repository: EventTypeRepository) = Flow[Event].map { event => Await.result(repository.increment(event.event_type), Duration.create(5, TimeUnit.SECONDS)); event }

  def incrementEventByWord(repository: EventsByWordRepository) = Flow[Event].map { event => Await.result(repository.increment(event.data), Duration.create(5, TimeUnit.SECONDS)); event }

  val redisIncrementEventType = incrementEventType(new RedisEventTypeRepository("192.168.59.100", 6379))

  val redisIncrementEventWord = incrementEventByWord(new RedisEventsByWordRepository("192.168.59.100", 6379))

//  val graph = GraphDSL.create() { implicit builder =>
//      import GraphDSL.Implicits._
//      val broadcast = builder.add(Broadcast[Event](2)) // the splitter - like a Unix tee
//      testSource ~> deserializeJSON ~> redisIncrementEventType ~> consoleTraceFlow ~> Sink.ignore
//      broadcast ~> redisIncrementEventWord ~> consoleTraceFlow ~> Sink.ignore // connect other side of splitter to console
//      ClosedShape
//  }
//  val materialized = RunnableGraph.fromGraph(graph).run()

  val memoryIncrementEventType = incrementEventType(MemoryEventTypeRepository)
  val memoryIncrementEventsByWord = incrementEventByWord(MemoryEventsByWordRepository)

//  testSource.
//    via(deserializeJSON).
//    via(redisIncrementEventType).
//    via(redisIncrementEventWord).
//    runWith(consoleSink).
//    onComplete(_ => system.shutdown())

  testSource.
    via(deserializeJSON).
    via(memoryIncrementEventType).
    via(memoryIncrementEventsByWord).
    runWith(consoleSink).
    onComplete { _ =>
      val future = for {
        eventsByType <- MemoryEventTypeRepository.getAll()
        eventsByWord <- MemoryEventsByWordRepository.getAll()
      } yield (eventsByType, eventsByWord)
      val result = Await.result(future, Duration.create(5, TimeUnit.SECONDS))
      println(result)
      system.shutdown()
    }


}
