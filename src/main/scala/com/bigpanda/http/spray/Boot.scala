package com.bigpanda.http.spray

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.Config
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem("bigpanda-system")

  /* Use Akka to create our Spray Service */
  val service = system.actorOf(Props[ServerActor], "bigpanda-service")

  /* and bind to Akka's I/O interface */
  val config = system.settings.config
  IO(Http) ! Http.Bind(service, config.getString("app.interface"), config.getInt("app.port"))

}

class ServerActor extends Actor with ActorRoute with ActorLogging with Configured {

  def actorRefFactory =
    context

  def receive =
    runRoute(sprayRoute)

  def config: Config =
    context.system.settings.config
}
