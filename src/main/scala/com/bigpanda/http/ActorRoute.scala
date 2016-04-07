package com.bigpanda.http

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.json4s._
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.duration._

/* Used to mix in Spray's Marshalling Support with json4s */
object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

/* Our case class, used for request and responses */
case class Foo(bar: String)

/* Our route directives, the heart of the service.
 * Note you can mix-in dependencies should you so chose */
trait ActorRoute extends HttpService {
  import Json4sProtocol._
  import WorkerActor._

  //These implicit values allow us to use futures
  //in this trait.
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  //Our worker Actor handles the work of the request.
  val worker = actorRefFactory.actorOf(Props[WorkerActor], "worker")

  val sprayRoute = {
    path("api/agregations") {
      get(complete(List(Foo("foo1"), Foo("foo2"))))
    }
  }

  def doCreate[T](foo: Foo) = {
    complete {
    //We use the Ask pattern to return
    //a future from our worker Actor,
    //which then gets passed to the complete
    //directive to finish the request.
    (worker ? Create(foo))
      .mapTo[Ok]
      .map(result => s"I got a response: ${result}")
      .recover { case _ => "error" }
    }
  }

}
