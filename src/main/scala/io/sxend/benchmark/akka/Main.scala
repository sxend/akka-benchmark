package io.sxend.benchmark.akka

import java.util.UUID

import akka.actor.ActorSelection
import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, SupervisorStrategy }
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ Cluster, Join, JoinSeedNodes, Subscribe }
import akka.actor.typed.scaladsl.AskPattern.{ Askable, schedulerFromActorSystem }
import akka.cluster.ClusterEvent.{ MemberEvent, MemberJoined }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity, EntityRef, EntityTypeKey }
import akka.util.Timeout
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.{ RandomStringUtils, StringUtils }

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object Main {
  implicit val timeout = Timeout(5 seconds)
  val TypeKey = EntityTypeKey[Echo.Command]("Echo")
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load
    println(config.getList("akka.cluster.seed-nodes"))
    implicit val system = ActorSystem(Behaviors.empty, "benchmark-system")
    import system.executionContext
    val cluster = Cluster(system)
    val sharding = ClusterSharding(system)
    if (cluster.selfMember.roles.contains("seed")) {
      val entityRef = sharding.entityRefFor(TypeKey, UUID.randomUUID().toString)
      val route =
        path("/endpoint") {
          get {
            onComplete(entityRef.ask(ref => Echo.Envelope(RandomStringUtils.randomAlphanumeric(10000), ref))) {
              case Success(response) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.message))
              case Failure(t) =>
                system.log.error(t.getMessage, t)
                reject
            }
          }
        }
      Http().newServerAt("localhost", 8080).bind(route)
    } else if (cluster.selfMember.roles.contains("worker")) {
      sharding.init(Entity(TypeKey)(createBehavior = entityContext => Behaviors.supervise(Echo(entityContext.entityId)).onFailure(SupervisorStrategy.resume)).withRole("worker"))
    }
  }
}
object Echo {
  sealed trait Command
  final case class Envelope(message: String, replyTo: ActorRef[Response]) extends Command with Serializable
  final case class Response(message: String)
  def apply(entityId: String): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case Envelope(message, replyTo) =>
      replyTo ! Response(s"reply[$entityId] $message")
      Behaviors.same
  }
}