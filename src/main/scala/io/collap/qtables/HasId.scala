package io.collap.qtables

trait HasId[A] extends HasKey {
  override type Key = Id[A]
  val id: Id[A]
  override def key = id
  def updateId(id: Id[A]): A
}
