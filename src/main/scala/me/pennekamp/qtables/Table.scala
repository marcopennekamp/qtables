package me.pennekamp.qtables

import scala.language.higherKinds

trait Table[A <: HasKey] extends ActionChecks {
  /**
    * The actual Quill Context.Quoted type.
    */
  type Quoted[Q]

  /**
    * The actual Quill Context.EntityQuery type.
    */
  type EntityQuery[T]

  /**
    * The query quote is declared here, so that the IDE (IntelliJ) can find
    * the qt symbol. This is currently a workaround for lackluster Scala macro
    * tooling in IDEs.
    *
    * We may write a plugin in the future, instead.
    */
  val qt: Quoted[EntityQuery[A]]

  /**
    * Finds the row identified by the key.
    */
  def qtFind: Quoted[A#Key => EntityQuery[A]]

  /**
    * This needs to be implemented by every table right now.
    * It's used to implement qtFind based on the key of the entity.
    */
  def qtFilterByKey: Quoted[A#Key => A => Boolean]

  /**
    * @return All rows in the database.
    */
  def list(): Seq[A]

  /**
    * Inserts a value into the database.
    * @return The inserted row.
    */
  def insert(value: A): A

  /**
    * Inserts a list of values into the database.
    */
  def insertMany(items: List[A]): List[A]

  /**
    * Updates the row corresponding to the key of the parameter.
    *
    * Note: Unless all attributes should be updated, using the more detailed update method should be preferred.
    *
    * @param value The instance whose row is supposed to be updated.
    */
  def update(value: A): Unit

  /**
    * Updates specific attributes of the row corresponding to the key.
    *
    * Note: Not yet implemented.
    */
  //def update(key: A#Key, f: (A => (Any, Any)), f2: (A => (Any, Any))*): Unit

  /**
    * Deletes the row corresponding to the key.
    */
  def delete(key: A#Key): Unit

  /**
    * @return The row with the specific key or None if no such row was found.
    */
  def find(key: A#Key): Option[A]
}
