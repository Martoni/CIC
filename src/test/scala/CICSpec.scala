package cic

import org.scalatest._
import chiseltest._
import chisel3._

class BasicTest extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "CIC"
  // test class body here
  it should "filter pdm signal file " in {
    println("hello")
  }
}
