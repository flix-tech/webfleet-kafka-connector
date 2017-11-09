package com.flixtech.transform

object WebfleetHelper {

  val NULL = "NULL"
  val ETA = "ETA"
  val NAVIGATION_STARTED = "NAVIGATION_STARTED"
  val UNKNOWN = "UNKNOWN"

  val MESSAGE_CLASS_MAP: Map[Int, String] = Map(
    1 -> "SYSTEM",
    2 -> "TEXT",
    3 -> "POLLING",
    4 -> "TIMER",
    5 -> "GPS",
    6 -> "INPUT",
    7 -> "OUTPUT",
    8 -> "DATA",
    9 -> "CONFIGURATION",
    10 -> "TRIP",
    11 -> "ORDER"
  )

  val ORDER_TYPE_MAP: Map[Int, String] = Map(
    1 -> "SERVICE",
    2 -> "PICKUP",
    3 -> "DELIVERY"
  )

  val MESSAGE_TYPE_MAP: Map[Int, String] = Map(
    353 -> "TRACKING",
    729 -> "SENT",
    737 -> "DELETED",
    738 -> "DELETED_ALL",
    751 -> "DESTINATION_REACHED",
    752 -> NAVIGATION_STARTED,
    753 -> "NAVIGATION_CANCELLED",
    754 -> "MESSAGE_RECEIVED",
    760 -> "STATE",
    761 -> "ETA",
    763 -> "NAVIGATION",
    710 -> "DRIVER_LOGON",
    711 -> "DRIVER_LOGOFF",
    712 -> "WORK_START",
    713 -> "WORK_END",
    714 -> "BREAK_START",
    715 -> "BREAK_END"
  )

  val ORDER_STATE_MAP: Map[Int, String] = Map(
    0 -> "NOT_YET_SENT",
    100 -> "OrderState.SENT",
    101 -> "RECEIVED",
    102 -> "READ",
    103 -> "ACCEPTED",
    241 -> "DELIVERY_ORDER_STARTED",
    298 -> "RESUMED",
    299 -> "SUSPENDED",
    301 -> "CANCELLED",
    302 -> "REJECTED",
    401 -> "FINISHED"
  )

  val GPS_STATUS_MAP: Map[String, String] = Map(
    "L" -> "LAST_KNOWN",
    "V" -> "NO_FIX",
    "A" -> "GOOD"
  )
}
