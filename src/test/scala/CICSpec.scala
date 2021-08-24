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

    val csvpath: String = "assets/flute_si_12_5Mhz.csv"

    it should "filter pdm signal file " + csvpath + s" and record $csvpath.pcm" in {
        println("Open file " + csvpath)
        val fcsv = io.Source.fromFile(csvpath)
        val itcsv = fcsv.getLines()
        // Get header titles
        val Array(atime, aclk, adata) = itcsv.next().split(',')
        println(s"time $atime, clk $aclk, data  $adata")
        val csvseq = itcsv.toSeq
        val csvseqsize = csvseq.size
        println(s"$csvpath have $csvseqsize lines")


        // convert csv to (clk,data) sequence
        val rawsig: Seq[(Int,Int)] = {
            csvseq.map { line =>
                val Array(btime, bclk, bdata) = line.split(',').map(_.trim.toInt)
                (bclk, bdata)
            }
        }

        // get pdm bitstream
        val pdm: Seq[Int]  = rawsig.zipWithIndex.map {
                case(a, i) => if(i != 0){ (rawsig(i-1),a) } else ((0,0), a)
            }.filter {
                case ((clkold, dataold), (clk, data)) => clkold == 0 && clk == 1
                case _ => false
            }.map{
                case ((_, _), (_, adata)) => adata
                case _ => 0
            }


        // test CIC
        val pcmFile = new BufferedOutputStream(new FileOutputStream(s"$csvpath.pcm"))
        //test(new CIC(R=68)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
        test(new CIC(R = 68)) { dut =>
            println("begin test")
            // init input
            dut.io.pdm.clk.poke(false.B)
            dut.io.pdm.data.poke(false.B)
            dut.clock.step(1)
            dut.io.pdm.clk.poke(true.B)
            dut.clock.step(1)
            var cyclenum = 0
            var pcmsamples = 0
            breakable {for(data <- pdm) {
                dut.io.pdm.clk.poke(false.B)
                dut.io.pdm.data.poke(data.B)
                dut.clock.step(1)
                dut.io.pdm.clk.poke(true.B)
                if(dut.io.pcm.valid.peek().litValue() == 1){
                    val pcmvalue = dut.io.pcm.bits.peek().litValue()
                    Stream.continually(pcmFile.write(pcmvalue.toByteArray))
//                    if(pcmsamples > 1000) {
//                      break
//                    }
                    pcmsamples += 1
                    if(pcmsamples % 100 == 0)
                      println(s"pcmsamples $pcmsamples -> $pcmvalue")
                }
                dut.clock.step(1)
                cyclenum += 1
            }}
        }
        pcmFile.close()
        fcsv.close()
    }
}
