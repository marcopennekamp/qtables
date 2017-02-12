package me.pennekamp.qtables

import io.getquill.MappedEncoding

/**
  * An ID type that guarantees that IDs can not be accidentally used as IDs of other, unrelated models.
  *
  * @tparam T The type of the model this type is an ID of.
  */
case class Id[T](value: Long) extends AnyVal {
  override def toString: String = value.toString
  def isDefined = value != Id.Undefined[T].value
}

object Id {
  def Undefined[A] = Id[A](-1)

  implicit def idEncoder[T] = MappedEncoding[Id[T], Long](_.value)
  implicit def idDecoder[T] = MappedEncoding[Long, Id[T]](Id(_))
}
