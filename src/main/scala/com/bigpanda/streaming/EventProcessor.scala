package com.bigpanda.streaming

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializerSettings, Supervision, ActorMaterializer}
import akka.stream.scaladsl._
import com.bigpanda.data.{EventTypeRepository, Event}

import org.json4s._
import org.json4s.native.Serialization

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

  val deserializeJSON = Flow[String].map(Serialization.read[Event])

  def incrementEventType(repository: EventTypeRepository) = Flow[Event].map { event => repository.increment(event.event_type) }


  testSource.
    via(deserializeJSON).
    runWith(consoleSink).
    onComplete(_ => system.shutdown())
}
