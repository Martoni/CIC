package cic

import org.scalatest._
import chiseltest._
import chisel3._

import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

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

        println("convert csv to (clk,data) sequence")
        val rawsig: Seq[(Int,Int)] = {
            csvseq.map { line =>
                val Array(btime, bclk, bdata) = line.split(',').map(_.trim.toInt)
                (bclk, bdata)
            }
        }

        println("get pdm bitstream")
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
        test(new CIC()).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
            println("begin test")
            // init input
            dut.io.pdm.clk.poke(false.B)
            dut.io.pdm.data.poke(false.B)
            dut.clock.step(1)
            dut.io.pdm.clk.poke(true.B)
            dut.clock.step(1)
            var cyclenum = 0
//            val pcmPrintFile = new PrintWriter(new File(s"$csvpath.pcm"))
            for(data <- pdm) {
                if(cyclenum % 1000 == 0)
                    println(s"cyclenum $cyclenum")
                dut.io.pdm.clk.poke(false.B)
                dut.io.pdm.data.poke(data.B)
                dut.clock.step(1)
                dut.io.pdm.clk.poke(true.B)
                dut.clock.step(1)
                if(dut.io.pcm.valid.peek().litValue() == 1){
                    println(dut.io.pcm.bits.peek().litValue())
                }
                cyclenum += 1
            }
        }
        fcsv.close()
    }
}
