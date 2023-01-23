package pl.kp.dontletexpire.model.view

import caliban.CalibanError
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.model.{Item, ItemStatus, Storage}
import pl.kp.dontletexpire.service.{QueryService, QueryServiceQueries, StatusService}
import pl.kp.dontletexpire.utils.TimelinePoint
import pl.kp.dontletexpire.utils.ZIOUtils.*
import zio.ZIO
import zio.query.ZQuery

case class ItemView(
    item: Item,
    status: ZIO[StatusService, CalibanError, ItemStatus],
    storage: ZQuery[QueryServiceQueries, CalibanError, Storage]
)

object ItemView:
  def from(now: TimelinePoint, item: Item): ItemView =
    ItemView(
      item,
      StatusService.itemStatus(now, item),
      QueryServiceQueries
        .storage(item.storageId)
        .someOrFail(AppError.result(s"Storage with id ${item.id} missing"))
        .errorAsCaliban
    )
end ItemView

case class ItemsView(resultSize: Int, time: TimelinePoint, items: List[ItemView])