package com.bigpanda.http

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem("bigpanda-system")

  /* Use Akka to create our Spray Service */
  val service = system.actorOf(Props[ServerActor], "bigpanda-service")

  /* and bind to Akka's I/O interface */
  IO(Http) ! Http.Bind(service, system.settings.config.getString("app.interface"), system.settings.config.getInt("app.port"))

}

/* Our Server Actor is pretty lightweight; simply mixing in our route trait and logging */
class ServerActor extends Actor with ActorRoute with ActorLogging {
  def actorRefFactory = context
  def receive = runRoute(sprayRoute)
}
