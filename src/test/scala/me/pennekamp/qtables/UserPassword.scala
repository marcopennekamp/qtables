package me.pennekamp.qtables

case class UserPassword(userId: Id[User], password: String) extends HasKey {
  override type Key = Id[User]
  override def key = userId
}
