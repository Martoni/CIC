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
        val title_line = itcsv.next().split(',')
        println()
        fcsv.close()
    }
}
