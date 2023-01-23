package pl.kp.dontletexpire.model

import scala.concurrent.duration.FiniteDuration

case class ItemStatus(`type`: ItemStatusType, millisTillDeadline: Long)

enum ItemStatusType:
  case Ok, NearlyExpired, Expired
