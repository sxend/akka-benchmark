package benchmark

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.Config

class Routes(env: {
  val handler: Handler
  val config: Config
}) {
  private val handler = env.handler
  private val askClient = (get & path("client"))(handler.askClient)
  private val askActor = (get & path("actor"))(handler.askActor)
  private val askBenchmark = pathPrefix("ask")(askClient ~ askActor)
  def asRoute: Route = askBenchmark
}
