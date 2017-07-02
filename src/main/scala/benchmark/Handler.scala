package benchmark

import java.util.UUID

import akka.pattern._
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.server.Directives.complete
import akka.cluster.client.ClusterClient._
import akka.util.Timeout

import scala.concurrent.duration._

class Handler(env: {
  val system: ActorSystem
  val echoActor: () => ActorRef
  val clusterClient: () => ActorRef
}) {
  implicit private val system = env.system
  implicit private val timeout = Timeout(10.seconds)
  private val echoActor = env.echoActor()
  private val clusterClient = env.clusterClient()
  def askClient = complete(clusterClient.ask(Send("/system/sharding/echo", uuid, localAffinity = false)).mapTo[String])
  def askActor = complete(echoActor.ask(uuid).mapTo[String])
  private def uuid = UUID.randomUUID().toString
}
