package benchmark

import benchmark.entity.Wrapper
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val format = jsonFormat1(Wrapper)
}

object JsonSupport extends JsonSupport