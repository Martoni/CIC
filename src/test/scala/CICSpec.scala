package cic

import org.scalatest._
import chiseltest._
import chisel3._

import scala.util.control.Breaks._
import java.io._ // for files read/write access
import scala.util.Random

class BasicTest extends FlatSpec with ChiselScalatestTester with Matchers {
    behavior of "CIC"

    val pdmpath: String = "assets/colamonptitfrere_12.5Ms.pdm"
    val runtime = Runtime.getRuntime
    val randbool = Random

    def printMemoryUsage(): Unit = {
        val mb = 1024*1024
        println("** Used Memory:  " + getMemoryUsage() + " MB")
        println("** Free Memory:  " + runtime.freeMemory/mb + "MB")
        println("** Total Memory: " + runtime.totalMemory/mb + "MB")
        println("** Max Memory:   " + runtime.maxMemory/mb + "MB")
    }

    def getMemoryUsage(): Long = {
        val mb = 1024*1024
        (runtime.totalMemory - runtime.freeMemory)/mb
    }

    it should "filter pdm signal file " + pdmpath + s" and record $pdmpath.pcm" in {
        println("Open file " + pdmpath)
        val bis = new BufferedInputStream(new FileInputStream(pdmpath))
        //val pdm = Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
        val pdm = for(i <- 0 to 24*1024*1024) yield if(randbool.nextBoolean) {1} else {0}
        //val pcmFile = new BufferedOutputStream(new FileOutputStream(s"$pdmpath.pcm"))

        // test CIC
        object MyCICParams extends CICParams(3, 68, 1)
        test(new CIC(c=MyCICParams)) { dut =>
        //test(new CIC(c=MyCICParams)) { dut =>
            println("begin test")
            // init input
            dut.io.pdm.clock.poke(false.B)
            dut.io.pdm.data.poke(false.B)
            dut.clock.step(1)
            dut.io.pdm.clock.poke(true.B)
            dut.clock.step(1)
            var cyclenum = 0
            var pcmsamples = 0
            breakable{for(data <- pdm) {
                dut.io.pdm.clock.poke(false.B)
                dut.io.pdm.data.poke(data.B)
                dut.clock.step(1)
                dut.io.pdm.clock.poke(true.B)
                if(dut.io.pcm.valid.peek().litValue() == 1){
                    val pcmvalue = dut.io.pcm.bits.peek().litValue()
                    val pcmarray = pcmvalue.toByteArray
                    //pcmFile.write((Array[Byte](0,0) ++ pcmarray).takeRight(2))
                    pcmsamples += 1
                    if(pcmsamples % 1000 == 0){
                      val memUsage = getMemoryUsage()
                      println(s"Mem: $memUsage,     |pcmsamples $pcmsamples -> $pcmvalue. Mem: $memUsage,")
                    }
                    if(pcmsamples > 80000)break;
                }
                dut.clock.step(1)
                cyclenum += 1
            }}
        }
        //pcmFile.close()
        bis.close
    }
}
