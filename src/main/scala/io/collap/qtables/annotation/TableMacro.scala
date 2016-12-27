package io.collap.qtables.annotation

import io.collap.qtables.Table

import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.whitebox

object TableMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val tableClassName = TypeName(classOf[Table[_]].getSimpleName)

    // FIXME: We should extract the entity type based on the type hierarchy, not a name.
    // Otherwise problems like this one appear: class UserTable extends TestTable[User] with Table[User]
    // We have to add a 'with Table[User]', because the macro doesn't see the TestTable as a Table.
    // A perhaps more elegant solution: Fetch the entity type of the query to get the entity type.
    def extractEntityType(parents: Seq[Trees#Tree]): Ident = {
      // The head of the parent list (extends ...) has to be treated in a special way.
      val q"${tq"${firstParentName: TypeName}[..$firstParentTargs]"}(...$_)" = parents.head

      if (firstParentName == tableClassName) {
        return firstParentTargs.head.asInstanceOf[Ident]
      }

      // Otherwise, the Table[T] declaration may be found in a 'with ...' declaration.
      // TODO: Test that this is working for 'with ...' as well.
      for (tree <- parents.tail) {
        val tq"${name: TypeName}[..$targs]" = tree
        if (name == tableClassName) {
          return targs.head.asInstanceOf[Ident]
        }
      }

      // If no 'with Table[T]' could be found, we need to abort the macro.
      c.abort(c.enclosingPosition, "\nThe @table annotation requires the class to be a subtype of Table." +
        "\nExample: class UserTable extends Table[User]." +
        "\nExample: class ATable extends Foo with Table[A]")
    }

    // Extracts the entity query from the annotation.
    val entityQuery = c.macroApplication match {
      case q"new $name($query).macroTransform(..$_)" => query
      case _ => c.abort(c.enclosingPosition, "\nThe @table annotation requires an entity query. " +
        "\nExample: @table(query[User])")
    }

    // FIXME: The update(key, _ -> _, ...) method doesn't seem to get compiled properly (should result in a dynamic query).

    val out = annottees.map(_.tree) match {
      case q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { ..$stats }" :: Nil =>
        val entityType = extractEntityType(parents)

        c.Expr[Any](
          q"""
          $mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            val qt = quote($entityQuery)

            def qtFindByKey = quote { (key: $entityType#Key) =>
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
