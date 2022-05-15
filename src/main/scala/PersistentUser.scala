package app.realworld

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.state.scaladsl.{DurableStateBehavior, Effect}
import app.realworld.CustomExceptions.AlreadyExistsException
import org.mindrot.jbcrypt.BCrypt

object PersistentUser {
  sealed trait Command extends CborSerializable
  final case class RegisterUser(username: String,
                                email: String,
                                password: String,
                                replyTo: ActorRef[StatusReply[User]])
      extends Command
  final case class GetUser(replyTo: ActorRef[StatusReply[User]]) extends Command

  // type alias to reduce boilerplate
  type ReplyEffect =
    akka.persistence.typed.state.scaladsl.ReplyEffect[Option[User]]

  final case class User(username: String,
                        email: String,
                        passwordHash: String,
                        bio: Option[String],
                        image: Option[String])
      extends CborSerializable {
    def applyCommand(command: Command): ReplyEffect =
      command match {
        case RegisterUser(_, _, _, replyTo) =>
          Effect.reply(replyTo)(
            StatusReply.error(
              AlreadyExistsException(("User already exists"))))
        case GetUser(replyTo) =>
          Effect.reply(replyTo)(StatusReply.success(this))
      }
  }

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("PersistentUser")

  def onFirstCommand(command: Command): ReplyEffect =
    command match {
      case RegisterUser(username, email, password, replyTo) =>
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        Effect
          .persist(
            Some(
              User(username = username,
                   email = email,
                   passwordHash = passwordHash,
                   bio = None,
                   image = None)
            ))
          .thenReply(replyTo)(newState => StatusReply.success(newState.value))
      case GetUser(replyTo) =>
        Effect.reply(replyTo)(StatusReply.error("User not found"))
    }

  def apply(username: String): Behavior[Command] =
    DurableStateBehavior.withEnforcedReplies[Command, Option[User]](
      persistenceId = PersistenceId.ofUniqueId(username),
      emptyState = None,
      commandHandler = (user, command) =>
        user match {
          case None       => onFirstCommand(command)
          case Some(user) => user.applyCommand(command)
      }
    )
}
