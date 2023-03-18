package org.quantumnest.server

import com.sun.net.httpserver._;
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.net.InetSocketAddress
import org.quantumnest.util.{Channel, MessageValue}

object Server {
  def start(commChannel: Channel[MessageValue.MessageShape]) {
    
    val server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/", new Server(commChannel));
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor()); // creates a default executor
    server.start();

  }
}

class Server(commChannel: Channel[MessageValue.MessageShape]) extends HttpHandler {
  private val routes: AtomicReference[Object] = new AtomicReference()
  private val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor() // Executors.newFixedThreadPool(10);
  override def handle(request: HttpExchange): Unit = {
    executor.submit(new Runnable {
      override def run(): Unit = threadedHandle(request)
    })
    return
  }

  private def threadedHandle(request: HttpExchange): Unit = {
    println("In thread handle")
    val method = request.getRequestMethod()
    val uri = request.getRequestURI()
    uri.getRawPath()
    println(f"Got ${uri.getRawPath()}")

    val response = f"This is the response ${uri.getRawPath()}\n\n";
    request.sendResponseHeaders(200, response.length())
    val os = request.getResponseBody()
    os.write(response.getBytes())
    os.close()
  }
}
