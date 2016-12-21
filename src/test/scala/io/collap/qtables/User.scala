package io.collap.qtables

case class User(id: Id[User], name: String) extends HasId[User] {
  override def updateId(id: Id[User]): User = copy(id = id)
}
