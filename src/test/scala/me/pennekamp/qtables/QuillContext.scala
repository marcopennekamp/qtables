package me.pennekamp.qtables

import java.io.Closeable
import javax.sql.DataSource

import io.getquill.{PostgresJdbcContext, SnakeCase}

/**
  * A global Quill context that will obviously not work at runtime, but for the sake of the tests
  * we are only interested in the compilation results right now.
  */
object QuillContext extends PostgresJdbcContext[SnakeCase](null: DataSource with Closeable)
