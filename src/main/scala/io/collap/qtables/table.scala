package io.collap.qtables

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros

/**
  * TODO: Document.
  *
  * @param entityQuery
  * @tparam T
  */
@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class table[T](entityQuery: Any) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro TableMacro.impl
}
