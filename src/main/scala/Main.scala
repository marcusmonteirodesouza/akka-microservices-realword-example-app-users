package app.realworld

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object Main {
  val logger = LoggerFactory.getLogger("users-service.Main")

  private def init(system: ActorSystem[_]): Unit = {
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem = system.toClassic
    implicit val ec = system.executionContext

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    PersistentUser.init(system)

    val routes = new Routes(system).routes

    val serverBinding =
      Http()(system)
        .newServerAt("0.0.0.0", 8080)
        .bind(routes)
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
    val system = ActorSystem[Nothing](Behaviors.empty, "users-service")

    try {
      init(system)
    } catch {
      case NonFatal(exception) =>
        logger.error("Terminating due to initialization failure.", exception)
        system.terminate()
    }
  }
}
