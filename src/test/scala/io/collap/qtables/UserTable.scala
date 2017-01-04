package io.collap.qtables

import QuillContext.{lift, query, _}
import io.collap.qtables.annotation.table

@table(query[User])
class UserTable extends TestTable[User] with Table[User] {
  override final def qtFilterByKey = quote((key: User#Key) => (e: User) => e.id == key)

  def test(): Seq[User] = {
    run(qt.filter(_.name == lift("Marco")))
  }
}
