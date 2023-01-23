package pl.kp.dontletexpire.db.repo

import cats.data.NonEmptyList
import cats.instances.list.*
import cats.syntax.foldable.*
import cats.syntax.option.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment
import pl.kp.dontletexpire.Types.{Limit, Offset}
import pl.kp.dontletexpire.db.*
import pl.kp.dontletexpire.model.{Item, ItemId, ItemInput, StorageId}

object ItemRepo
    extends GenericRepo[ItemId, ItemInput, Item](
      "item",
      NonEmptyList.of(
        ItemColumns.storageId,
        ItemColumns.name,
        ItemColumns.description,
        ItemColumns.quantity,
        ItemColumns.deadline
      )
    ):
  def updateQuantity(id: ItemId, quantity: Int): ConnectionIO[Int] =
    update(id, ItemColumns.quantity.set(quantity))
  def topItemsByDeadline(
      storages: List[StorageId],
      offset: Option[Offset],
      limit: Option[Limit]
  ): ConnectionIO[List[Item]] =
    val query: Option[Fragment] =
      NonEmptyList.fromList(storages).map(ids => Fragments.in(ItemColumns.storageId.fragment, ids))
    select(
      SelectParams
        .withQuery(query)
        .withSort(ItemColumns.deadline.sortAscending, ItemColumns.quantity.sortDescending)
        .withOffset(offset)
        .withLimit(limit)
    )
  end topItemsByDeadline
end ItemRepo

object ItemColumns extends ColumnSupport[ItemInput]:
  val storageId   = column("storage_id", _.storageId)
  val name        = column("name", _.name)
  val description = column("description", _.description)
  val quantity    = column("quantity", _.quantity)
  val deadline    = column("deadline", _.deadline)
end ItemColumns
