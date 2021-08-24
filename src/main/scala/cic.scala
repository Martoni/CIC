package cic

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

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
           val width: Int = 16, // output size
           val clkEdge: ClockEdge = Rising //Data valid on clk edge
          )
  extends Module {
  val io = IO(new Bundle {
    val pdm = Input(new PDM())
    val pcm = Valid(SInt(width.W))
  })

  println(c) // display parameters values

  /* Integrator pulse of one module clock cycle, at specified edge of pdm clock input */
  val  pdm_pulse = RegNext(clkEdge match {
    case Rising => io.pdm.clk & (!RegNext(io.pdm.clk))
    case Falling => !io.pdm.clk & RegNext(io.pdm.clk)
  }, false.B)

  /* Decimator pulse of one module clock cycle */
  val (sampleCount, dec_pulse) = Counter(pdm_pulse, c.decRatio)

  /* CIC implementation */

  /* sample pdm data on pdm_pulse for first integrator stage */
  val pdm_bit = RegEnable(Mux(io.pdm.data, 1.S, -1.S), 1.S(width.W), pdm_pulse)

  val itgrOutput = {
    /* Integrator stage definition */
    def getItgrStage(inp: SInt): SInt = {
      val itgr  = RegInit(0.S(width.W))
      when(pdm_pulse)
      {
        itgr := itgr + inp
      }
      itgr
    }

    /* integrator stages */
    val itgr_outputs = Wire(Vec(c.nStages, SInt(width.W)))

    itgr_outputs(0) := getItgrStage(pdm_bit)
    1 until c.nStages foreach {i=>
      itgr_outputs(i) := getItgrStage(itgr_outputs(i - 1))
    }
    itgr_outputs.reverse.head
  }

  io.pcm.bits := {
    /* Comb stage definition */
    def getCombStage(inp: SInt): SInt = {
      val delayed_value = ShiftRegister( inp, c.combDelay, 0.S(width.W), dec_pulse)
      RegEnable(inp - delayed_value, 0.S(width.W), dec_pulse)
    }

    /* comb stages */
    val comb_outputs = Wire(Vec(c.nStages, SInt(width.W)))

    comb_outputs(0) := getCombStage(itgrOutput)
    1 until c.nStages foreach {i=>
      comb_outputs(i) := getCombStage(comb_outputs(i - 1))
    }
    comb_outputs.reverse.head
  }

  /* PCM valid */
  io.pcm.valid := dec_pulse
}

object CICDriver extends App {
  println("Some scala tests")
  val c = CICParams(5, 32, 1)
  print(c)
  println("Generate CIC verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog"),
    Seq(ChiselGeneratorAnnotation(() => new CIC())))
}
