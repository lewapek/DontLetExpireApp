package pl.kp.dontletexpire.db.repo

import cats.data.NonEmptyList
import cats.instances.list.*
import cats.syntax.foldable.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment
import pl.kp.dontletexpire.db.{ColumnSupport, GenericRepo}
import pl.kp.dontletexpire.model.{Storage, StorageId, StorageInput}

object StorageRepo
    extends GenericRepo[StorageId, StorageInput, Storage](
      "storage",
      NonEmptyList.of(StorageColumns.name, StorageColumns.description)
    )

object StorageColumns extends ColumnSupport[StorageInput]:
  val name        = column("name", _.name)
  val description = column("description", _.description)
end StorageColumns
