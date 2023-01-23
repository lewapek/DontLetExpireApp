package pl.kp.dontletexpire.model

import pl.kp.dontletexpire.utils.NewEntityType

object StorageId extends NewEntityType
type StorageId = StorageId.Type

case class Storage(
    id: StorageId,
    name: String,
    description: Option[String]
)

case class StorageInput(
    name: String,
    description: Option[String]
)
