package quantum_nest.http

import com.sun.net.httpserver._;
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

class Server extends HttpHandler {
  private val routes: AtomicReference[Object] = new AtomicReference()
  private val executor: ExecutorService = Executors.newFixedThreadPool(10);
  override def handle(request: HttpExchange): Unit = {
    // use threadpool... when JDK 19+ is supported, turn this into
    //
    executor.submit(new Runnable {
      override def run(): Unit = threadedHandle(request)
    })
    return
  }

  private def threadedHandle(request: HttpExchange): Unit = {
    val method = request.getRequestMethod()
    val uri = request.getRequestURI()
    uri.getRawPath()
    return
  }
}
