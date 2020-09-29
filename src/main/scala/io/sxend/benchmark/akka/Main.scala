package io.sxend.benchmark.akka

import java.util.UUID

import akka.actor.ActorSelection
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{Cluster, Join, JoinSeedNodes, Subscribe}
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.cluster.ClusterEvent.{MemberEvent, MemberJoined}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}

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
      sharding.init(Entity(TypeKey)(createBehavior = entityContext => Behaviors.supervise(Echo(entityContext.entityId)).onFailure(SupervisorStrategy.resume)).withRole("worker"))
    } else if(cluster.selfMember.roles.contains("worker")) {
      val entityRef = sharding.entityRefFor(TypeKey, UUID.randomUUID().toString)
    }
  }
}
object Echo {
  sealed trait Command
  final case class Envelope(message: String, replyTo: ActorRef[String]) extends Command with Serializable

  def apply(entityId: String): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case Envelope(message, replyTo) =>
      replyTo ! s"reply[$entityId] $message"
      Behaviors.same
  }
}