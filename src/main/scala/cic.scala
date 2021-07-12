package cic

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage

class PDM extends Bundle {
    val clk = Bool()
    val data = Bool()
}

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


    io.pcm.valid := DontCare
    io.pcm.bits := DontCare
}

object CICDriver extends App {
    println("Generate CIC verilog")
    (new ChiselStage).emitVerilog(new CIC())
}
