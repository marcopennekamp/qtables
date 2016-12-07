package io.collap.qtables

import QuillContext.{lift, query, _}

@table[User](query[User])
class UserTable {
  def test(): Seq[User] = {
    run(qt.filter(_.name == lift("Marco")))
  }
}
