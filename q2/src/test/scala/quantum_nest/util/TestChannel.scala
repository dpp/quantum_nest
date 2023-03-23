package quantum_nest.util

import org.quantumnest.util._
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import net.liftweb.common.{Box, Full, Empty}
import java.util.concurrent.atomic.AtomicInteger

class TestChannel extends munit.FunSuite {
  val chan = Channel[String]()

  test("Send/Receive Message") {

    val message = UUID.randomUUID().toString()
    val sync = new Object()
    var answer = "foo"
    val got = new AtomicBoolean(false)
    chan.send(message)
    Thread
      .ofVirtual()
      .start(() => {
        val a = chan.receive(56000)

        sync.synchronized {
          got.set(true)
          answer = a.openOr("FAILED")
          sync.notifyAll()
        }
      })

    sync.synchronized {
      while (!got.get()) {
        sync.wait()
      }
      assertEquals(answer, message)
    }
  }

  test("Channel lookup") {
    val mc = Channel.locateChannel[String](chan.getUUID())
    val fc: Box[Channel[String]] = Full(chan)
    assertEquals(fc, mc)
  }

  test("Channel find different type should be empty") {
    val mc = Channel.locateChannel[Long](chan.getUUID())

    assertEquals(Empty.asInstanceOf[Box[Channel[Long]]], mc)
  }

  test("Channel timeout") {
    val shouldBeEmpty = chan.receive(10)
    assertEquals(Empty.asInstanceOf[Box[String]], shouldBeEmpty)
  }

  test("10000 channel timeouts") {
    val count = new AtomicInteger(0)
    val max = 10000
    (1 to max)
      .map(i => {
        val t = Thread
          .ofVirtual()
          .start(() => {
            val a = chan.receive(1)

            if (a.isEmpty) {
              count.addAndGet(1)
            }

          })
        t
      })
      .foreach(t => t.join())

    assertEquals(max, count.get())

  }
}
