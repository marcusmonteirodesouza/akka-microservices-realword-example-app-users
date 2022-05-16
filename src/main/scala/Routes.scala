package app.realworld

import CustomExceptions.AlreadyExistsException

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, NullOptions, RootJsonFormat}

import scala.concurrent.Future

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

final case class ErrorResponse(errors: ErrorResponseErrors)
final case class ErrorResponseErrors(body: Seq[String])

trait JsonFormats
    extends SprayJsonSupport
    with DefaultJsonProtocol
    with NullOptions {
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

  implicit val errorResponseErrorsFormat: RootJsonFormat[ErrorResponseErrors] =
    jsonFormat1(ErrorResponseErrors)
  implicit val errorResponseFormat: RootJsonFormat[ErrorResponse] = jsonFormat1(
    ErrorResponse)
}

class Routes(system: ActorSystem[_]) extends Directives with JsonFormats {
  private implicit val timeout: Timeout = Timeout.create(
    system.settings.config.getDuration("users-service.ask-timeout"))

  private val sharding = ClusterSharding(system)

  implicit def customExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case AlreadyExistsException(message, _) =>
        complete(
          StatusCodes.UnprocessableEntity,
          ErrorResponse(errors = ErrorResponseErrors(body = Seq(message))))
    }

  private def registerUser(username: String,
                           email: String,
                           password: String): Future[PersistentUser.User] = {
    val userRef = sharding.entityRefFor(PersistentUser.TypeKey, username)
    userRef.askWithStatus(
      replyTo =>
        PersistentUser.RegisterUser(username = username,
                                    email = email,
                                    password = password,
                                    replyTo = replyTo))
  }

  val routes: Route = Route.seal(concat(pathEndOrSingleSlash {
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
  }))
}
