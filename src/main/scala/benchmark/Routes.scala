package benchmark

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.Config

class Routes(env: {
  val handler: Handler
  val config: Config
}) {
  private val handler = env.handler
  private val hello = (get & path("hello"))(handler.hello)
  private val receptionist = (get & path("receptionist"))(handler.askReceptionist)
  private val actor = (get & path("actor"))(handler.askActor)
  private val askBenchmark = pathPrefix("ask")(receptionist ~ actor)
  def asRoute: Route = hello ~ askBenchmark
}
