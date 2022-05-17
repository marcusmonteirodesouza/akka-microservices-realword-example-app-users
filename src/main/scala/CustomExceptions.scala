package app.realworld

object CustomExceptions {
  final case class AlreadyExistsException(message: String = "",
                                          cause: Throwable = None.orNull)
      extends Exception(message, cause)
      with CborSerializable
}
