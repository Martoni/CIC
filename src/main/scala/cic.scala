package cic

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage

class PDM extends Bundle {
    val clk = Bool()
    val data = Bool()
}

class Pouet(beuh: Int)

class CIC (val width: Int = 16, // output size
           val rising: Boolean = true,
           val N: Int = 5,      // stage number
           val R: Int = 32,     // decimation factor
           val M: Int = 1)      // Order of the filter (number of samples per stage)
           extends Module {
    val io = IO(new Bundle {
        val pdm = Input(new PDM())
        val pcm = Valid(SInt(width.W))
    })

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

    io.pcm.valid := DontCare
    io.pcm.bits := DontCare
}

object CICDriver extends App {
    println("Generate CIC verilog")
    (new ChiselStage).emitVerilog(new CIC())
}
