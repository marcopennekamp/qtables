package io.collap.qtables

import QuillContext.{lift, query, _}
import io.collap.qtables.annotation.table

@table(query[User])
class UserTable extends Table[User] {
  def test(): Seq[User] = {
    run(qt.filter(_.name == lift("Marco")))
  }
}
