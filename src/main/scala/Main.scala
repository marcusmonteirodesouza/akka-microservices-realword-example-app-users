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

  private def init()(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    PersistentUser.init(system)

    val routes = new Routes(system).routes

    val serverBinding =
      Http()(system)
        .newServerAt(system.settings.config.getString("users-service.host"),
                     system.settings.config.getInt("users-service.port"))
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
    val system = ActorSystem[Nothing](Behaviors.empty, "usersService")

    try {
      init()(system)
    } catch {
      case NonFatal(exception) =>
        logger.error("Terminating due to initialization failure.", exception)
        system.terminate()
    }
  }
}
