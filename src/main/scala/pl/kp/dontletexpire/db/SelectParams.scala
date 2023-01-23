package pl.kp.dontletexpire.db

import cats.syntax.option.*
import doobie.{Fragment, Write}
import io.circe.{Decoder, Encoder}
import pl.kp.dontletexpire.Types.{Limit, Offset}
import pl.kp.dontletexpire.utils.NewType

case class SelectParams(
    query: Option[Fragment],
    sort: List[Fragment],
    offset: Option[Offset],
    limit: Option[Limit]
):
  def withQuery(query: Fragment): SelectParams                   = copy(query = query.some)
  def withSort(first: Fragment, others: Fragment*): SelectParams = copy(sort = first :: others.toList)
  def withOffset(n: Offset): SelectParams                        = copy(offset = n.some)
  def withLimit(n: Limit): SelectParams                          = copy(limit = n.some)
  def withOffset(n: Option[Offset]): SelectParams                = copy(offset = n)
  def withLimit(n: Option[Limit]): SelectParams                  = copy(limit = n)
end SelectParams

object SelectParams:
  val empty                                                      = SelectParams(None, List.empty, None, None)
  def withQuery(query: Fragment): SelectParams                   = empty.withQuery(query)
  def withQuery(query: Option[Fragment]): SelectParams           = empty.copy(query = query)
  def withSort(first: Fragment, others: Fragment*): SelectParams = empty.withSort(first, others: _*)
end SelectParams
