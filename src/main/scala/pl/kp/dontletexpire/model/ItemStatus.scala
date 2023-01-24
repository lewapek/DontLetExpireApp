package pl.kp.dontletexpire.model

case class ItemStatus(`type`: ItemStatusType, millisTillDeadline: Long)

enum ItemStatusType:
  case Ok, NearlyExpired, Expired
