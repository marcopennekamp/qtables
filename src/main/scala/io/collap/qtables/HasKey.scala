package io.collap.qtables

/**
  * [[HasKey]] can be added to a database object type, where an instance of that type is a single row
  * in the database.
  */
trait HasKey {
  type Key

  /**
    * @return The key of the specific row represented by this object.
    */
  def key: Key
}
