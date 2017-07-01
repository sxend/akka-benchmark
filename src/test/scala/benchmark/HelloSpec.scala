package benchmark

import org.specs2.mutable._
import akka.http.scaladsl.testkit.Specs2RouteTest
import com.typesafe.config.ConfigFactory

class HelloSpec extends Specification with Specs2RouteTest {
  private val config = ConfigFactory.load
  "/hello" should {
    "GET" in {
      "respond hello json" in {
        Get("/hello") ~> Routes(config) ~> check {
          responseAs[String] shouldEqual """{"message":"hello"}"""
        }
      }
    }
  }
}
