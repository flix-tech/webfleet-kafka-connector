package com.flixtech.transform

import java.time.Instant
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.flixtech.Message._

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by fditrani on 08/09/2017.
  */
object JsonToAvroTransformer {

  import WebfleetHelper._

  private val mapper = new ObjectMapper()

  def jsonToMessage(jsonString: String): Try[WebfleetMessage] = {
    Try {
      val strippedJson = jsonString.stripMargin.stripLineEnd
      val jsonNode = mapper.readTree(strippedJson).asInstanceOf[ObjectNode]

      val messageTypeCode = parseInt(jsonNode, "msg_type").get % 1000
      val msgTimeString = parseString(jsonNode, "msg_time").get
      val messageType = lookupMessageType(messageTypeCode)
      val (maybeOrderData, maybeOrderEtaData) = parseOrderDataAndEta(jsonNode, messageType)
      val maybeObjectUid = parseString(jsonNode, "objectuid")

      WebfleetMessage(
        msg_time = isoUtcTimestampToMillis(msgTimeString),
        msg_time_str = msgTimeString,
        msgid = parseLong(jsonNode, "msgid").get,
        msg_class = lookupMessageClass(parseInt(jsonNode, "msg_class")),
        msg_type = messageType,
        msg_type_code = messageTypeCode,
        vehicle_data = parseVehicleData(jsonNode),
        msg_text = parseString(jsonNode, "msg_text"),
        object_no = parseString(jsonNode, "objectno").get,
        order_data = maybeOrderData,
        order_eta_data = maybeOrderEtaData,
        navigation_data = parseNavigationData(jsonNode, messageType),
        position_data = parsePositionData(jsonNode),
        surplus_data = parseSurplusData(jsonNode),
        objectuid = maybeObjectUid,
        unmatched_data = parseUnmatchedData(jsonNode)

      )
    }
  }

  private def parseString(jsonNode: ObjectNode, fieldName: String): Option[String] =
    Option(jsonNode.remove(fieldName)).map(_.asText)

  private def parseInt(jsonNode: ObjectNode, fieldName: String): Option[Int] =
    Option(jsonNode.remove(fieldName)).map(_.asInt)

  private def parseDouble(jsonNode: ObjectNode, fieldName: String): Option[Double] =
    Option(jsonNode.remove(fieldName)).map(_.asDouble)

  private def parseLong(jsonNode: ObjectNode, fieldName: String): Option[Long] =
    Option(jsonNode.remove(fieldName)).map(_.asLong)

  private def parseUtcTimestamp(jsonNode: ObjectNode, fieldName: String): Option[Long] =
    Option(jsonNode.remove(fieldName)).map(_.asText()).map(isoUtcTimestampToMillis)

  private def isoUtcTimestampToMillis(timestamp: String): Long =
    Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(timestamp)).toEpochMilli

  private def parseVehicleData(jsonNode: ObjectNode): Option[VehicleData] =
    VehicleData.create(
      parseInt(jsonNode, "odometer"),
      parseInt(jsonNode, "speed")
    )

  private def parseNavigationData(jsonNode: ObjectNode, messageType: String): Option[NavigationData] = {
    messageType match {
      case NAVIGATION_STARTED =>
        Some(NavigationData(
          dest_text = parseString(jsonNode, "dest_text"),
          eta = EtaData.create(parseUtcTimestamp(jsonNode, "eta"), parseInt(jsonNode, "distance"))
        ))
      case _ => None
    }
  }

  private def parsePositionData(jsonNode: ObjectNode): Option[PositionData] = {
    def createPositionData(latitude: Double) = {
      PositionData(
        gpsStatus = lookupGpsStatus(parseString(jsonNode, "status")),
        pos_time = parseUtcTimestamp(jsonNode, "pos_time"),
        pos_latitude = Some(latitude / 1000000),
        pos_longitude = parseDouble(jsonNode, "pos_longitude").map(_ / 1000000),
        pos_country = parseString(jsonNode, "pos_country"),
        pos_postcode = parseString(jsonNode, "pos_postcode"),
        pos_text = parseString(jsonNode, "pos_text"),
        pos_params = parseString(jsonNode, "pos_params"),
        course = parseInt(jsonNode, "course"),
        direction = parseInt(jsonNode, "direction"),
        pos_addrno = parseInt(jsonNode, "pos_addrno")
      )
    }

    parseDouble(jsonNode, "pos_latitude").map(createPositionData)
  }

  private def parseSurplusData(jsonNode: ObjectNode): Option[Map[String, String]] = {
    def createEntryMap(surplusData: JsonNode) = {
      surplusData.fields.asScala.foldLeft(Map[String, String]())((currMap, entries) => {
        currMap.updated(entries.getKey, entries.getValue.asText)
      })
    }

    Option(jsonNode.remove("surplus_data")).map(createEntryMap)
  }

  private def parseUnmatchedData(jsonNode: ObjectNode): Option[Map[String, String]] = {
    val map = jsonNode.fields.asScala.foldLeft(Map[String, String]())((currMap, entries) => {
      currMap.updated(entries.getKey, entries.getValue.asText)
    })
    if (map.nonEmpty) Some(map) else None
  }

  private def parseOrderDataAndEta(jsonNode: ObjectNode, messageType: String): (Option[OrderData], Option[EtaData]) = {
    def parseOrderData: Option[OrderData] = {
      Option(
        OrderData(
          orderno = parseString(jsonNode, "orderno"),
          order_state = lookupOrderState(parseInt(jsonNode, "order_state")),
          order_state_code = parseInt(jsonNode, "order_state_code"),
          order_type = lookupOrderType(parseInt(jsonNode, "order_type"))
        )
      )
    }

    def parseETAData: Option[EtaData] = {
      messageType match {
        case ETA =>
          EtaData.create(
            eta = parseUtcTimestamp(jsonNode, "eta"),
            distance = parseInt(jsonNode, "distance")
          )
        case _ => None
      }
    }

    def parseOrderEtaData: (Option[OrderData], Option[EtaData]) = {
      (parseOrderData, parseETAData)
    }

    if (jsonNode.has("orderno")) parseOrderEtaData else (None, None)
  }

  private def lookupMessageClass(code: Option[Int]) = lookup(MESSAGE_CLASS_MAP, code)

  private def lookupOrderType(code: Option[Int]): String = lookup(ORDER_TYPE_MAP, code)

  private def lookupOrderState(code: Option[Int]): String = lookup(ORDER_STATE_MAP, code)

  private def lookupGpsStatus(code: Option[String]): String = lookup(GPS_STATUS_MAP, code)

  private def lookup[A, B >: String](map: Map[A, B], key: Option[A]): B = key.flatMap(map.get).getOrElse(UNKNOWN)

  private def lookupMessageType(code: Int): String = MESSAGE_TYPE_MAP.getOrElse(code, UNKNOWN)
}
