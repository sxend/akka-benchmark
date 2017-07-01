package benchmark

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object Bootstrap {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("benchmark")
    implicit val materializer = ActorMaterializer()
    val config = system.settings.config
    Http().bindAndHandleAsync(
      Route.asyncHandler(Routes(config)),
      config.getString("benchmark.server.host"),
      config.getInt("benchmark.server.port"),
      parallelism = config.getInt("benchmark.server.parallelism")
    )
  }
}

