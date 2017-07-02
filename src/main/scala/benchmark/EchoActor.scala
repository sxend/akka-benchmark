package benchmark

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.cluster.client.{ ClusterClient, ClusterClientReceptionist, ClusterClientSettings }
import akka.cluster.sharding.ShardRegion.{ HashCodeMessageExtractor, MessageExtractor }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

class EchoActor extends Actor with ActorLogging {
  def receive = {
    case msg =>
      log.info(s"incoming message from ${sender()}")
      sender() ! msg
  }
}

object EchoActor {
  val typeName = "echo"
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg => (msg.toString, msg)
  }
  val extractShardId: ShardRegion.ExtractShardId = {
    case msg => msg.toString
  }
  private var client: ActorRef = _
  def shardClient(implicit system: ActorSystem) = {
    if (client == null) {
      client = system.actorOf(ClusterClient.props(ClusterClientSettings(system)))
    }
    client
  }
  def startRegion(implicit system: ActorSystem) = {
    registerService(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = Props[EchoActor],
      settings = ClusterShardingSettings(system).withRole("worker"),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
  }
  def shardProxy(implicit system: ActorSystem) = {
    registerService(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = Some("worker"),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
  }
  private def registerService(service: ActorRef)(implicit system: ActorSystem): ActorRef = {
    ClusterClientReceptionist(system).registerService(service)
    service
  }
}