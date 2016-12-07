package io.collap.qtables

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object TableMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // Extracts the entity type and entity query from the annotation.
    val (entityType, entityQuery) = c.macroApplication match {
      case q"new $name[..$tparams]($query).macroTransform(..$_)" =>
        // The annotation expects exactly one type parameter. We need to make sure that this is the case.
        if (tparams.length != 1) {
          c.abort(c.enclosingPosition, "The @table annotation requires an entity type, which can currently not be inferred. " +
            "Example: @table[User](query[User])")
        }
        (tparams.head, query)
      case _ => c.abort(c.enclosingPosition, "The @table annotation requires an entity query. " +
        "Example: @table[User](query[User])")
    }

    val out = annottees.map(_.tree) match {
      case q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { ..$stats }" :: Nil =>
        c.Expr[Any](
          q"""
          $mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            type T = $entityType
            val qt = quote($entityQuery)

            ..$stats
          }
          """
        )
      case _ => c.abort(c.enclosingPosition, "The annottee of @table must be any class.")
    }

    println(out.toString())

    out
  }
}
