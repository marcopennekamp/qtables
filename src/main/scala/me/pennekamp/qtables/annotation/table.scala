package me.pennekamp.qtables.annotation

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros

/**
  * TODO: Document.
  *
  * @param entityQuery
  */
@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class table(entityQuery: Any) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro TableMacro.impl
}
