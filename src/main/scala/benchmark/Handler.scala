package benchmark

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.complete
import benchmark.entity.Wrapper
import JsonSupport._

class Handler(env: {
  val system: ActorSystem
}) {
  def hello = complete(Wrapper("hello"))
  def askReceptionist = ???
  def askActor = ???
}
