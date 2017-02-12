package me.pennekamp.qtables.annotation

import me.pennekamp.qtables.{HasId, Table}

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
    def extractEntityTypeTree(parents: Seq[Trees#Tree]): Ident = {
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

    val out = annottees.map(_.tree) match {
      case q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { ..$stats }" :: maybeObject =>
        // We want to match any companion object that might be attached to the table class
        // and output it without transforming it.
        val companion = maybeObject match {
          case Nil => q""
          case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" :: Nil =>
            q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }"
        }

        val entityTypeTree = extractEntityTypeTree(parents)
        val entityType = c.typecheck(entityTypeTree, c.TYPEmode).tpe

        // Depending on whether the entity type has an ID, we have to return the generated ID.
        // TODO: Test/Validate that these insertion methods are generated correctly and work correctly.
        val insertMethods = if (entityType <:< typeOf[HasId[_]]) {
          // Here, the entity has an ID.
          Seq (
            q"""
            override def insert(value: $entityTypeTree): $entityTypeTree = {
              val id = run(qt.insert(lift(value)).returning(_.id))
              value.updateId(id)
            }
            """,
            q"""
            override def insertMany(items: List[$entityTypeTree]): List[$entityTypeTree] = {
              val ids = run(liftQuery(items).foreach(c => qt.insert(c).returning(_.id)))
              items.zip(ids).map { case (item, id) => item.updateId(id) }
            }
            """
          )
        } else {
          // In this case, there is just a key which does not need to be generated by the database.
          Seq (
            q"""
            override def insert(value: $entityTypeTree): $entityTypeTree = {
              run(qt.insert(lift(value))).checkInsertion()
              value
            }
            """,
            q"""
            override def insertMany(items: List[$entityTypeTree]): List[$entityTypeTree] = {
              run(liftQuery(items).foreach(c => qt.insert(c))).checkInsertions()
              items
            }
            """
          )
        }

        val classExpr = q"""
          $mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            val qt = quote($entityQuery)

            override def qtFind = quote { (key: $entityTypeTree#Key) =>
              qt.filter(e => qtFilterByKey(key)(e))
            }

            override def list() = run(qt)

            ..$insertMethods

            override def update(value: $entityTypeTree): Unit = {
              run(qtFind(lift(value.key)).update(lift(value))).checkUpdate()
            }

            override def delete(key: $entityTypeTree#Key): Unit = {
              run(qtFind(lift(key)).delete).checkDeletion()
            }

            override def find(key: $entityTypeTree#Key): Option[$entityTypeTree] = {
              run(qtFind(lift(key))).headOption
            }

            ..$stats
          }
        """
        // This method isn't part of Table, since it has a Quoted as a parameter.
        //def findOne(p: Quoted[($entityTypeTree) => Boolean]): Option[$entityTypeTree] = {
        //  run(qt.filter(e => p(e))).headOption
        //}

        // TODO: We should support findOne without making it a dynamic query (if possible).
        c.Expr[Any](
          if (companion.isEmpty) {
            classExpr
          } else {
            q"""
            $classExpr
            $companion
            """
          }
        )
      case x => c.abort(c.enclosingPosition, "The annottee of @table must be any class.\nGot: " + x.toString())
    }

    //println(out.toString())

    out
  }
}