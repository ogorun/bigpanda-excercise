package com.bigpanda.http.spray

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.bigpanda.http.core.{DefaultWorkerObjectFactory, WorkerActor}
import org.json4s._
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.duration._

/* Used to mix in Spray's Marshalling Support with json4s */
object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

/* Our route directives, the heart of the service.
 * Note you can mix-in dependencies should you so chose */
trait ActorRoute extends HttpService {
  this: Configured =>

  import Json4sProtocol._
  import WorkerActor._

  //These implicit values allow us to use futures
  //in this trait.
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  //Our worker Actor handles the work of the request.
  val factory = new DefaultWorkerObjectFactory(config)
  val worker = actorRefFactory.actorOf(Props.create(classOf[WorkerActor], factory), "worker")

  val sprayRoute = {
    path("api" / "aggregations") {
      get {
        getAggregations
      }
    }
  }

  def getAggregations[T] = complete {
  //We use the Ask pattern to return
  //a future from our worker Actor,
  //which then gets passed to the complete
  //directive to finish the request.
    (worker ? Aggregations)
    .mapTo[Ok]
    .map { case Ok(aggregation) => aggregation }
    .recover { case _ => "error" }
  }

}
