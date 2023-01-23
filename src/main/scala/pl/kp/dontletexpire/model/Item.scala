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
):
  def incrementQuantity(n: Int): Item =
    val updatedQuantity = quantity + n
    copy(quantity = if updatedQuantity < 0 then 0 else updatedQuantity)
  end incrementQuantity
end Item

case class ItemInput(
    storageId: StorageId,
    name: String,
    description: Option[String],
    quantity: Int,
    deadline: TimelinePoint
)
