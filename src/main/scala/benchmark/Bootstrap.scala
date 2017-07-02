package benchmark

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.Config

object Bootstrap {
  def main(args: Array[String]): Unit = {
    implicit val _system = ActorSystem("benchmark")
    val _config: Config = _system.settings.config
    _config.getString("benchmark.role") match {
      case "http" =>
        val env = new {
          val system: ActorSystem = _system
          val config: Config = _config
          val echoActor: () => ActorRef = () => EchoActor.shardProxy
          val echoActorClient: () => ActorRef = () => EchoActor.shardClient
          val handler: Handler = new Handler(this)
          val routes: Routes = new Routes(this)
        }
        startServer(
          env.routes.asRoute,
          env.config.getString("benchmark.server.host"),
          env.config.getInt("benchmark.server.port"),
          env.config.getInt("benchmark.server.parallelism")
        )
      case "seed"   => EchoActor.shardProxy
      case "worker" => EchoActor.startRegion
    }
  }
  private def startServer(route: Route, host: String, port: Int, parallelism: Int)(implicit system: ActorSystem) = {
    implicit val materializer = ActorMaterializer()
    Http().bindAndHandleAsync(
      Route.asyncHandler(route), host, port, parallelism = parallelism)
  }

}

