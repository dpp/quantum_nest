package org.quantumnest

import org.quantumnest.server.Server
import org.quantumnest.util.SimpleChannel

/**
 * @author ${user.name}
 */
object App {
  
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    println( "Hello World!" )
    println("concat arguments = " + foo(args))
    Server.start(new SimpleChannel())
    while (true) {
      Thread.sleep(50)
    }
  }

}
