package app.realworld

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.{Failure, Success}

object Main {
  private def startHttpServer(routes: Route)(
      implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val serverBinding = Http().newServerAt("localhost", 8080).bind(routes)
    serverBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/",
                        address.getHostString,
                        address.getPort)
      case Failure(exception) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system",
                         exception)
        system.terminate()
    }
  }
  def main(args: Array[String]): Unit = {
    val guardian = Behaviors.setup[Nothing] { context =>
      val routes = new Routes(context.system)
      startHttpServer(routes.routes)(context.system)
      Behaviors.empty
    }

    ActorSystem[Nothing](guardian, "usersServer")
  }
}
