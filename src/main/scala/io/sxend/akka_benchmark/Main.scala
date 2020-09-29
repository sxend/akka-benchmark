package io.sxend.akka_benchmark

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
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object Main {
  implicit val timeout = Timeout(5 seconds)
  val TypeKey = EntityTypeKey[Counter.Command]("Counter")
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load
    println(config.getList("akka.cluster.seed-nodes"))
    implicit val system = ActorSystem(Behaviors.empty, "benchmark-system")
    import system.executionContext
    val cluster = Cluster(system)
    val sharding = ClusterSharding(system)
    val shardRegion: ActorRef[ShardingEnvelope[Counter.Command]] =
      sharding.init(Entity(TypeKey)(createBehavior = entityContext => Behaviors.supervise(Counter(entityContext.entityId)).onFailure(SupervisorStrategy.resume)).withRole("worker"))

    val subscriber = system.systemActorOf(Behaviors.setup[MemberEvent] { context =>
      Behaviors.receiveMessage[MemberEvent] {
        case MemberJoined(member) =>
          println(s"member joined: $member")
          Behaviors.same
        case event =>
          println(s"member event: $event")
          Behaviors.same
      }
    }, "member-event")
    cluster.subscriptions ! Subscribe(subscriber, classOf[MemberEvent])
    system.scheduler.scheduleAtFixedRate(0.seconds, 10.seconds)(new Runnable {
      override def run(): Unit = {
        println(cluster.state.leader)
        println(cluster.state.members)
        val entityRef = sharding.entityRefFor(TypeKey, "counter-1")
        entityRef ! Counter.Increment
        entityRef.ask(Counter.GetValue).onComplete {
          case Success(value)     => println(s"now count: $value")
          case Failure(exception) => println(exception)
        }
      }
    })
  }
}
object Counter {
  sealed trait Command
  case object Increment extends Command with Serializable
  final case class GetValue(replyTo: ActorRef[Int]) extends Command with Serializable

  def apply(entityId: String): Behavior[Command] = {
    def updated(value: Int): Behavior[Command] = {
      Behaviors.receiveMessage[Command] {
        case Increment =>
          println("Increment!!!")
          updated(value + 1)
        case GetValue(replyTo) =>
          println("GetValue!!")
          replyTo ! value
          Behaviors.same
      }
    }

    updated(0)

  }
}