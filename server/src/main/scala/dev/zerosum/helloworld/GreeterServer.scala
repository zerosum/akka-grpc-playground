package dev.zerosum.helloworld

import akka.actor.ActorSystem
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import dev.zerosum.helloworld.grpc._

import scala.concurrent.{ExecutionContext, Future}

object GreeterServer {
  def main(args: Array[String]): Unit = {
    // Important: enable HTTP/2 in ActorSystem's config
    // We do it here programmatically, but you can also set it in the application.conf
    val conf =
      ConfigFactory.parseString("akka.http.server.enable-http2 = on").withFallback(ConfigFactory.defaultApplication())
    val system = ActorSystem("HelloWorld", conf)
    new GreeterServer(system).run()
    // ActorSystem threads will keep the app alive until `system.terminate()` is called
  }
}

class GreeterServer(system: ActorSystem) {
  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val sys: ActorSystem     = system
    implicit val ec: ExecutionContext = sys.dispatcher

    val greeterService    = GreeterServiceHandler.partial(new GreeterServiceImpl())
    val reflectionService = ServerReflection.partial(List(GreeterService))

    // Create service handlers
    val service: HttpRequest => Future[HttpResponse] =
      ServiceHandler.concatOrNotFound(
        greeterService,
        reflectionService
      )

    // Bind service handler servers to localhost:8080/8081
    val binding = Http().newServerAt("127.0.0.1", 8080).bind(service)

    // report successful binding
    binding.foreach { binding => println(s"gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}
