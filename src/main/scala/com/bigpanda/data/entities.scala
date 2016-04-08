package com.bigpanda.data

/**
  * Created by olgagorun on 07/04/2016.
  */

case class Event(event_type: String, data: String, timestamp: Long)

case class EventTypeCount(eventType: String, eventsCount: Int)

case class WordCount(word: String, eventsCount: Int)


