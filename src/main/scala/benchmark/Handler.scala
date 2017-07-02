package benchmark

import java.util.UUID

import akka.pattern._
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.server.Directives.complete
import JsonSupport._
import akka.cluster.client.ClusterClient
import akka.util.Timeout

import scala.concurrent.duration._

class Handler(env: {
  val system: ActorSystem
  val echoActor: () => ActorRef
  val echoActorClient: () => ActorRef
}) {
  implicit private val system = env.system
  import system.dispatcher
  implicit private val timeout = Timeout(10.seconds)
  private val echoActor = env.echoActor()
  private val echoActorClient = env.echoActorClient()
  def askClient = complete(echoActorClient.ask(ClusterClient.Send("/system/sharding/echo", uuid, localAffinity = true)).mapTo[String])
  def askActor = complete(echoActor.ask(uuid).mapTo[String])
  private def uuid = UUID.randomUUID().toString
}
