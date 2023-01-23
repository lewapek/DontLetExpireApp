package pl.kp.dontletexpire.model

import pl.kp.dontletexpire.utils.{TimelinePoint, NewEntityType}

object ItemId extends NewEntityType
type ItemId = ItemId.Type

case class Item(
    id: ItemId,
    storageId: StorageId,
    name: String,
    description: Option[String],
    quantity: Int,
    deadline: TimelinePoint
)

case class ItemInput(
    storageId: StorageId,
    name: String,
    description: Option[String],
    quantity: Int,
    deadline: TimelinePoint
)
