package io.collap.qtables

import QuillContext._
import io.collap.qtables.annotation.table

@table(query[UserPassword])
class UserPasswordTable extends TestTable[UserPassword] with Table[UserPassword] {

}
