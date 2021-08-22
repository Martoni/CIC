package cic

import org.scalatest._
import chiseltest._
import chisel3._

class BasicTest extends FlatSpec with ChiselScalatestTester with Matchers {
    behavior of "CIC"

    val csvpath: String = "assets/flute_si_12_5Mhz.csv"

    it should "filter pdm signal file " + csvpath in {
        println("Open file " + csvpath)
        val fcsv = io.Source.fromFile(csvpath)
        val itcsv = fcsv.getLines()
        // Get header titles
        val Array(atime, aclk, adata) = itcsv.next().split(',')
        println(s"time $atime, clk $aclk, data  $adata")

        // convert csv to (clk,data) iterator
        val rawsig: Iterator[(Int,Int)] =
            itcsv.map{ line =>
                val Array(btime, bclk, bdata) = line.split(',').map(_.trim.toInt)
                (bclk, bdata)
            }
        fcsv.close()

        val (cclk, cdata) = rawsig.next()
        print(s"cclk $cclk, cdata $cdata\n")
    }
}
