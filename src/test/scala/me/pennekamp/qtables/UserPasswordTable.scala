package me.pennekamp.qtables

import QuillContext._
import me.pennekamp.qtables.annotation.table

@table(query[UserPassword])
class UserPasswordTable extends TestTable[UserPassword] with Table[UserPassword] {
  override final def qtFilterByKey = quote((key: UserPassword#Key) => (e: UserPassword) => e.userId == key)
}
