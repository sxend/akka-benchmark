package benchmark

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.Config

object Bootstrap {
  def main(args: Array[String]): Unit = {
    val env = new {
      implicit val system = ActorSystem("benchmark")
      implicit val materializer = ActorMaterializer()
      val config: Config = system.settings.config
      val handler = new Handler(this)
      val routes = new Routes(this)
    }
    Http().bindAndHandleAsync(
      Route.asyncHandler(env.routes.asRoute),
      env.config.getString("benchmark.server.host"),
      env.config.getInt("benchmark.server.port"),
      parallelism = env.config.getInt("benchmark.server.parallelism")
    )
  }
}

