package io.collap.qtables

/**
  * [[HasKey]] describes a database row that can be uniquely found with a key.
  */
trait HasKey {
  type Key

  /**
    * @return The key of the specific row represented by this object.
    */
  def key: Key
}
