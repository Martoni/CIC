package cic

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import scala.math.pow

class PDM extends Bundle {
  val clk = Bool()
  val data = Bool()
}

case class CICParams(nStages: Int, decRatio: Int, combDelay: Int){
  override def toString: String = s"N = $nStages, R = $decRatio, M = $combDelay"
}

object DefaultCICParams extends CICParams(5, 32, 1)

sealed trait ClockEdge
case object Rising extends ClockEdge
case object Falling extends ClockEdge

class CIC (val c : CICParams = DefaultCICParams,
           val clkEdge: ClockEdge = Rising //Data valid on clk edge
          )
  extends Module {
  val regSize = log2Ceil(pow(c.decRatio*c.combDelay, c.nStages).toInt) - 1
  val io = IO(new Bundle {
    val pdm = Input(new PDM())
    val pcm = Valid(SInt(regSize.W))
  })

  println(c) // display parameters values
  println(s"regSize: $regSize")

  /* Integrator pulse of one module clock cycle, at specified edge of pdm clock input */
  val  pdm_pulse = RegNext(clkEdge match {
    case Rising => io.pdm.clk & (!RegNext(io.pdm.clk))
    case Falling => !io.pdm.clk & RegNext(io.pdm.clk)
  }, false.B)

  /* Decimator pulse of one module clock cycle */
  val (sampleCount, dec_pulse) = Counter(pdm_pulse, c.decRatio)

  /* CIC implementation */

  /* sample pdm data on pdm_pulse for first integrator stage */
  val pdm_bit = RegEnable(Mux(io.pdm.data, 1.S, -1.S), 1.S(regSize.W), pdm_pulse)

  val itgrOutput = {
    /* Integrator stage definition */
    def getItgrStage(inp: SInt): SInt = {
      val itgr  = RegInit(0.S(regSize.W))
      when(pdm_pulse)
      {
        itgr := itgr + inp
      }
      itgr
    }

    /* integrator stages */
    val itgr_outputs = Wire(Vec(c.nStages, SInt(regSize.W)))

    itgr_outputs(0) := getItgrStage(pdm_bit)
    1 until c.nStages foreach {i=>
      itgr_outputs(i) := getItgrStage(itgr_outputs(i - 1))
    }
    itgr_outputs.reverse.head
  }

  io.pcm.bits := {
    /* Comb stage definition */
    def getCombStage(inp: SInt): SInt = {
      val delayed_value = ShiftRegister( inp, c.combDelay, 0.S(regSize.W), dec_pulse)
      RegEnable(inp - delayed_value, 0.S(regSize.W), dec_pulse)
    }

    /* comb stages */
    val comb_outputs = Wire(Vec(c.nStages, SInt(regSize.W)))

    comb_outputs(0) := getCombStage(itgrOutput)
    1 until c.nStages foreach {i=>
      comb_outputs(i) := getCombStage(comb_outputs(i - 1))
    }
    comb_outputs.reverse.head
  }

  /* PCM valid */
  io.pcm.valid := RegNext(dec_pulse)
}

object CICDriver extends App {
  println("Some scala tests")
  val c = CICParams(3, 68, 1)
  println(c)
  println("Generate CIC verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog"),
    Seq(ChiselGeneratorAnnotation(() => new CIC(c=c))))
}
