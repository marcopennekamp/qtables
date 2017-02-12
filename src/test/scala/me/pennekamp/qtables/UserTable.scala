package me.pennekamp.qtables

import QuillContext.{lift, query, _}
import me.pennekamp.qtables.annotation.table

@table(query[User])
class UserTable extends TestTable[User] with Table[User] {
  override final def qtFilterByKey = quote((key: User#Key) => (e: User) => e.id == key)

  def test(): Seq[User] = {
    run(qt.filter(_.name == lift("Marco")))
  }
}

object UserTable {
  val exampleTable = new UserTable
}
