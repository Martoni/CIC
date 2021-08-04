package chiselintro

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage

object Count {
  var counter: Int = 0
  def inc(): Unit = {
    println("Compteur : " + counter)
    counter += 1
  }
}

class SimpleCounter extends Module {
  val io = IO(new Bundle {
    val count = Output(UInt(8.W))
  })

  val countReg = RegInit(0.U(8.W))
  countReg := countReg + 1.U
  io.count := countReg
}



object ExampleDriver extends App {
    println("Compteur")
    val count1 = Count
    count1.inc()
    val count2 = Count
    count2.inc()

    println("Generate simple counter example")
    (new ChiselStage).emitVerilog(new SimpleCounter)
}
