package cic

import org.scalatest._
import chiseltest._
import chisel3._

import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation
import scala.util.control.Breaks._
import java.io._ // for files read/write access

class BasicTest extends FlatSpec with ChiselScalatestTester with Matchers {
    behavior of "CIC"

    val pdmpath: String = "assets/colamonptitfrere_12.5Ms.pdm"

    it should "filter pdm signal file " + pdmpath + s" and record $pdmpath.pcm" in {
        println("Open file " + pdmpath)
        val bis = new BufferedInputStream(new FileInputStream(pdmpath))
        val pdm = Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
        bis.close

        // test CIC
        object MyCICParams extends CICParams(3, 68, 1)
        val pcmFile = new BufferedOutputStream(new FileOutputStream(s"$pdmpath.pcm"))
        test(new CIC(c=MyCICParams)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
        //test(new CIC(c=MyCICParams)) { dut =>
            println("begin test")
            // init input
            dut.io.pdm.clk.poke(false.B)
            dut.io.pdm.data.poke(false.B)
            dut.clock.step(1)
            dut.io.pdm.clk.poke(true.B)
            dut.clock.step(1)
            var cyclenum = 0
            var pcmsamples = 0
            breakable{for(data <- pdm) {
                dut.io.pdm.clk.poke(false.B)
                dut.io.pdm.data.poke(data.B)
                dut.clock.step(1)
                dut.io.pdm.clk.poke(true.B)
                if(dut.io.pcm.valid.peek().litValue() == 1){
                    val pcmvalue = dut.io.pcm.bits.peek().litValue()
                    val pcmarray = pcmvalue.toByteArray
                    pcmFile.write((Array[Byte](0,0) ++ pcmarray).takeRight(2))
                    pcmsamples += 1
                    if(pcmsamples % 1000 == 0)
                      println(s"pcmsamples $pcmsamples -> $pcmvalue")
                    if(pcmsamples > 45000)break;
                }
                dut.clock.step(1)
                cyclenum += 1
            }}
        }
        pcmFile.close()
    }
}
