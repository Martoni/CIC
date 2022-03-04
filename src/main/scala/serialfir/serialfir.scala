// See README.md for license details.
package serialfir

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}


case class SerialFIRParams(val tapnum: Int, val insize: Int, val outsize: Int)

object defaultSerialFIRParams extends
                SerialFIRParams(tapnum = 200,
                                insize = 16,
                                outsize = 0)

class SerialFIR (val p : SerialFIRParams = defaultSerialFIRParams) extends Module {
  val boutsize = if(p.outsize == 0)(log2Ceil(p.tapnum) + 2*p.insize) else p.outsize
  val io = IO(new Bundle {
    // Input signal
    val sgin    = Flipped(Valid(SInt(p.insize.W)))
    // Input coefficients
    val coin = Input(SInt(p.insize.W))
    // Output
    val sgout = Valid(SInt(boutsize.W))
  })

  println("  SerialFIR parameters:")
  println("   - tapnum : " + p.tapnum)
  println("   - insize : " + p.insize)
  println("   - outsize: " + boutsize)
  //val samplesMem = Mem(p.tapnum, SInt(p.insize.W))
  val samplesMem = Mem((1<<log2Ceil(p.tapnum))-1, SInt(p.insize.W))
  val samples_ptr = RegInit((p.tapnum - 1).U)
  val real_ptr = RegInit((p.tapnum - 1).U)
  val tapcnt = RegInit(p.tapnum.U)

  val accu_size = log2Ceil(p.tapnum) + 2*p.insize
  val accu = RegInit(0.S(accu_size.W))
  println("   - accusize: " + accu_size)

  /* States machine */
  val s_init::s_compute::s_validout::Nil = Enum(3)
  val stateReg = RegInit(s_init)
  switch(stateReg){
    is(s_init) {
      when(io.sgin.valid) {
        stateReg := s_compute
        real_ptr := samples_ptr
      }
    }
    is(s_compute) {
      when(tapcnt === 1.U) {
        stateReg := s_validout
      }
    }
    is(s_validout) {
      stateReg := s_init
    }
  }

  when(stateReg === s_init) {
    accu := 0.S
    tapcnt := p.tapnum.U
  }

  when(stateReg === s_compute) {
    when(tapcnt =/= 0.U){
      when(real_ptr === 0.U){
        real_ptr := (p.tapnum - 1).U
      }.otherwise {
        real_ptr := real_ptr - 1.U
      }
      tapcnt := tapcnt - 1.U
    }
    accu := accu + samplesMem(real_ptr) * io.coin
  }

  /* Samples input */
  when(io.sgin.valid){
    when(samples_ptr === (p.tapnum - 1).U){
      samples_ptr := 0.U
    }.otherwise {
      samples_ptr := samples_ptr + 1.U
    }
    samplesMem(samples_ptr) := io.sgin.bits
  }

  // Outputs
  if(p.outsize == 0){
    io.sgout.bits := accu
  } else {
    io.sgout.bits := accu(accu_size-1, accu_size-p.outsize).asSInt
  }
  io.sgout.valid := RegNext(stateReg === s_validout)
}

object SerialFIR extends App {
  println("*******************************")
  println("* Generate 200 taps SerialFIR *")
  println("*******************************")
  object c extends SerialFIRParams(tapnum = defaultSerialFIRParams.tapnum,
                                   insize = defaultSerialFIRParams.insize,
                                   outsize = defaultSerialFIRParams.outsize)
  (new chisel3.stage.ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(ChiselGeneratorAnnotation(() => new SerialFIR(
          SerialFIRParams(tapnum=200,
                          insize=c.insize,
                          outsize=16))))
    )

}
