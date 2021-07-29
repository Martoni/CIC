package cic

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage

class PDM extends Bundle {
    val clk = Bool()
    val data = Bool()
}

//class CICParam(val N: Int, val R, val M)

//class CICParam(aN: Int, aR: Int, aM: Int){
//    val N: Int = aN
//    val R: Int = aR
//    val M: Int = aM

trait DisplayCICParam {
  val N: Int; val R: Int; val M: Int
  def display(): Unit = {
      println("N = " + N)
      println("R = " + R)
      println("M = " + M)
    }
}

case class CICParam(N: Int, R: Int, M: Int) extends DisplayCICParam

class CIC (val width: Int = 16, // output size
           val rising: Boolean = true,
           val N: Int = 5,      // stage number
           val R: Int = 32,     // decimation factor
           val M: Int = 1)      // Order of the filter (number of samples per stage)
           extends Module with DisplayCICParam {
    val io = IO(new Bundle {
        val pdm = Input(new PDM())
        val pcm = Valid(SInt(width.W))
    })

    display() // display N, R, M parameters values

    /* detect pdm_clk edge */
    val  pdm_edge = RegInit(false.B)
    if(rising) {
        pdm_edge := io.pdm.clk & (!RegNext(io.pdm.clk))
    } else {
        pdm_edge := !io.pdm.clk & RegNext(io.pdm.clk)
    }

    /* get pdm bit data */
    val pdm_bit = RegInit(1.S(width.W))
    when(pdm_edge) {
        pdm_bit := Mux(io.pdm.data, 1.S, -1.S)
    }


    /* Integrator stages */
    val itgr  = RegInit(VecInit(Seq.fill(N){0.S(width.W)}))
    when(pdm_edge){
      itgr.zipWithIndex.foreach {
        case(itgvalue, 0) =>
          itgr(0) := itgr(0) + pdm_bit
        case(itgvalue, i) =>
          itgr(i) := itgr(i) + itgr(i-1)
      }
    }

    /* Downsampler */
   val sampleCount = RegInit(R.U)
   val dec_pulse = RegInit(false.B)

   dec_pulse := false.B
   when(pdm_edge){
      when(sampleCount === 0.U){
        sampleCount := R.U
        dec_pulse := true.B
      }.otherwise {
        sampleCount := sampleCount - 1.U
      }
    }

    /* Comb filter stages */
    val comb_reg = RegInit(VecInit(Seq.fill(N){0.S(width.W)}))
    val delayed_values = RegInit(VecInit(Seq.fill(N){0.S(width.W)}))
    comb_reg.zipWithIndex.foreach {
      case(cbreg, 0) => {
        delayed_values(0) := ShiftRegister(itgr(N-1), M, dec_pulse)
        when(dec_pulse) {
          comb_reg(0) := itgr(N-1) - delayed_values(0)
        }
      }
      case(cbreg, i) => {
        delayed_values(i) := ShiftRegister(comb_reg(i-1), M, dec_pulse)
        when(dec_pulse) {
          comb_reg(i) := comb_reg(i-1) - delayed_values(i)
        }
      }
    }

    io.pcm.valid := dec_pulse
    io.pcm.bits := comb_reg(N-1)
}

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


object CICDriver extends App {
    println("Some scala tests")
    val cicpar = CICParam(5, 32, 1)
    cicpar.display()
    println("Compteur")
    val count1 = Count
    count1.inc()
    val count2 = Count
    count2.inc()
    println("Generate CIC verilog")
    (new ChiselStage).emitVerilog(new CIC())

    println("Generate simple counter example")
    (new ChiselStage).emitVerilog(new SimpleCounter)
}
