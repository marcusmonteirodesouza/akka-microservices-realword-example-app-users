package app.realworld

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.state.scaladsl.{
  DurableStateBehavior,
  Effect,
  ReplyEffect
}
import org.mindrot.jbcrypt.BCrypt

object PersistentUser {
  sealed trait Command extends CborSerializable
  final case class RegisterUser(username: String,
                                email: String,
                                password: String,
                                replyTo: ActorRef[StatusReply[User]])
      extends Command
  final case class GetUser(replyTo: ActorRef[StatusReply[User]]) extends Command

  final case class User(id: String,
                        username: String,
                        email: String,
                        passwordHash: String,
                        bio: Option[String],
                        image: Option[String])
      extends CborSerializable

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("PersistentUser")

  def commandHandler: (User, Command) => ReplyEffect[User] =
    (user, command) =>
      command match {
        case RegisterUser(username, email, password, replyTo) =>
          val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
          Effect
            .persist(
              user.copy(username = username,
                        email = email,
                        passwordHash = passwordHash))
            .thenReply(replyTo)(newState => StatusReply.success(newState))
        case GetUser(replyTo) =>
          Effect.reply(replyTo)(StatusReply.success(user))
    }

  def apply(userId: String, persistenceId: PersistenceId): Behavior[Command] =
    DurableStateBehavior.withEnforcedReplies[Command, User](
      persistenceId = persistenceId,
      emptyState = User(id = userId,
                        username = "",
                        email = "",
                        passwordHash = "",
                        bio = None,
                        image = None),
      commandHandler = commandHandler
    )
}
