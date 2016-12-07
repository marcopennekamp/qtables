package io.collap.qtables.`macro`

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.meta._

import scala.collection.immutable.Seq

/**
  * TODO: Document.
  *
  * @param entityQuery
  */
@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class table(entityQuery: Any) extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    def transform(self: Tree, mods: Seq[Mod], tname: Type.Name, tparams: Seq[Type.Param],
      ctorMods: Seq[Mod], paramss: Seq[Seq[Term.Param]], withTypes: Seq[Ctor.Call], body: Seq[Stat]) = {
      // First we need to extract the entity query tree.
      val entityQuery = self match {
        case q"new $_($q)" => q
        case _ => abort("The @table annotation requires an entity query. Example: @table(query[User])")
      }

      println(withTypes.structure)

      // We also need to extract the entity type from the Table[A] template.
      val entityTypes = withTypes.map {
        case Term.Apply(Term.ApplyType(Ctor.Ref.Name("Table"), Seq(tpe)), Nil) => Some(tpe)
        case _ => None
      }.filter(_.isDefined).map(_.get)

      if (entityTypes.isEmpty) {
        abort("The @table annotation requires an entity type, which can currently not be inferred. " +
          "Example: @table(query[User]) class UserTable extends Table[User]")
      }

      if (entityTypes.length > 1) {
        abort("The @table annotation requires a unique entity type. You may extend Table[A] only once. " +
          "Example: @table(query[User]) class UserTable extends Table[User]")
      }

      val entityType = entityTypes.head

      q"""
    ..$mods class $tname[..$tparams] ..$ctorMods(...$paramss) extends ${withTypes.head} with ..${withTypes.tail} {
      val qt = quote($entityQuery)
      ..$body
    }
    """
    }

    val out = defn match {
      case q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template with ..$tt { ..$body }" =>
        transform(this, mods, tname, tparams, ctorMods, paramss, template +: tt, body)
      case q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template with ..$tt" =>
        transform(this, mods, tname, tparams, ctorMods, paramss, template +: tt, Nil)
      case _ => abort("The annottee of @table must be a class.")
    }

    println(out.syntax)

    out
  }
}
