package app.realworld

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

final case class User(username: String,
                      email: String,
                      token: String,
                      bio: Option[String],
                      image: Option[String])

final case class RegisterUserRequest(user: RegisterUserRequestUser)
final case class RegisterUserRequestUser(username: String,
                                         email: String,
                                         password: String)
final case class RegisterUserResponse(user: User)

final case class GetCurrentUserResponse(user: User)

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat5(User)

  implicit val registerUserRequestUserFormat
    : RootJsonFormat[RegisterUserRequestUser] = jsonFormat3(
    RegisterUserRequestUser)
  implicit val registerUserRequestFormat: RootJsonFormat[RegisterUserRequest] =
    jsonFormat1(RegisterUserRequest)
  implicit val registerUserResponseFormat
    : RootJsonFormat[RegisterUserResponse] = jsonFormat1(RegisterUserResponse)

  implicit val getCurrentUserResponseFormat
    : RootJsonFormat[GetCurrentUserResponse] = jsonFormat1(
    GetCurrentUserResponse)
}

class Routes(system: ActorSystem[_]) extends Directives with JsonFormats {
  private implicit val timeout: Timeout = 10.seconds

  private val sharding = ClusterSharding(system)

  sharding.init(Entity(typeKey = PersistentUser.TypeKey) { entityContext =>
    PersistentUser(entityContext.entityId,
                   PersistenceId.ofUniqueId(entityContext.entityId))
  })

  private def registerUser(username: String,
                           email: String,
                           password: String): Future[PersistentUser.User] = {
    val userId = UUID.randomUUID().toString
    val userRef = sharding.entityRefFor(PersistentUser.TypeKey, userId)
    userRef.askWithStatus(
      replyTo =>
        PersistentUser.RegisterUser(username = username,
                                    email = email,
                                    password = password,
                                    replyTo = replyTo))
  }

  val routes: Route =
    concat(pathEndOrSingleSlash {
      concat {
        post {
          entity(as[RegisterUserRequest]) {
            request =>
              onSuccess(
                registerUser(username = request.user.username,
                             email = request.user.email,
                             password = request.user.password)) { user =>
                val response = RegisterUserResponse(
                  user = User(username = user.username,
                              email = user.email,
                              token = "",
                              bio = user.bio,
                              image = user.image))
                complete(StatusCodes.Created, response)
              }
          }
        }
      }
    })
}
