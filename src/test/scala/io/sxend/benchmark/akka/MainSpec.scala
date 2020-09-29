package io.sxend.benchmark.akka

import org.scalatest._
import flatspec._
import matchers._

class MainSpec extends AnyFlatSpec with should.Matchers {
  "The Hello object" should "say hello" in {
    Main.TypeKey.name shouldEqual "Echo"
  }
}
