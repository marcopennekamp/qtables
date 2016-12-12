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

      // TODO: We need to check that the entity type is a subtype of HasKey, but this doesn't seem to be possible with scalameta right now (without a semantic API).

      // FIXME: The update(key, _ -> _, ...) method doesn't seem to get compiled properly (should result in a dynamic query).
      q"""
      ..$mods class $tname[..$tparams] ..$ctorMods(...$paramss) extends ${withTypes.head} with ..${withTypes.tail} {
        val qt = quote($entityQuery)

        def qtFindByKey = quote { key: $entityType#Key =>
          qt.filter(e => e.key == key)
        }

        override def list() = run(qt)

        override def insert(value: $entityType): $entityType = {
          run(qt.insert(lift(value))).checkInsertion()
          value
        }

        override def insertMany(items: List[$entityType]): Unit = {
          run(liftQuery(items).foreach(c => qt.insert(c))).checkInsertions()
        }

        override def update(value: $entityType): Unit = {
          run(qtFindByKey(lift(value.key)).update(lift(value))).checkUpdate()
        }

        override def update(key: $entityType#Key, f: (($entityType) => (Any, Any)), f2: (($entityType) => (Any, Any))*): Unit = {
          run(qtFindByKey(lift(key)).update(f, f2: _*)).checkUpdate()
        }

        override def delete(key: $entityType#Key): Unit = {
          run(qtFindByKey(lift(key)).delete).checkDeletion()
        }

        override def find(key: $entityType#Key): Option[$entityType] = {
          run(qtFindByKey(lift(key))).headOption
        }

        // This method isn't part of Table, since it has a Quoted as a parameter.
        def findOne(p: Quoted[($entityType) => Boolean]): Option[$entityType] = {
          run(qt.filter(e => p(e))).headOption
        }

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
