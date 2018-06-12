package com.flixtech

import com.flixtech.kafka.WebfleetSchema._
import org.apache.kafka.connect.data.{Schema, Struct}

object Message {

  implicit class PutOptional(x: Struct) {

    def putOptionalStruct(fieldName: String, item: Option[ConnectMessage]): Struct = {
      item match {
        case Some(value) => x.put(fieldName, value.struct)
        case None => x
      }
    }

    def putOptional[A](fieldName: String, item: Option[A]): Struct = {
      item match {
        case Some(value) => x.put(fieldName, value)
        case None => x
      }
    }
  }

  sealed trait ConnectMessage {
    val schema: Schema

    def struct: Struct
  }

  case class PositionData(
                           gpsStatus: String,
                           pos_time: Option[Long] = None,
                           pos_latitude: Option[Double] = None,
                           pos_longitude: Option[Double] = None,
                           pos_country: Option[String] = None,
                           pos_postcode: Option[String] = None,
                           pos_addrno: Option[Int] = None,
                           pos_params: Option[String] = None,
                           pos_text: Option[String] = None,
                           course: Option[Int] = None,
                           direction: Option[Int] = None) extends ConnectMessage {
    override val schema: Schema = PositionDataSchema

    override def struct: Struct = {
      new Struct(schema)
        .put("gps_status", gpsStatus)
        .putOptional("pos_time", pos_time)
        .putOptional("pos_latitude", pos_latitude)
        .putOptional("pos_longitude", pos_longitude)
        .putOptional("pos_country", pos_country)
        .putOptional("pos_postcode", pos_postcode)
        .putOptional("pos_addrno", pos_addrno)
        .putOptional("pos_params", pos_params)
        .putOptional("pos_text", pos_text)
        .putOptional("course", course)
        .putOptional("direction", direction)
    }
  }

  case class OrderData(
                        orderno: Option[String] = None,
                        order_state: String,
                        order_state_code: Option[Int] = None,
                        order_type: String) extends ConnectMessage {
    override val schema: Schema = OrderDataSchema

    override def struct: Struct = {
      new Struct(schema)
        .putOptional("orderno", orderno)
        .put("order_state", order_state)
        .putOptional("order_state_code", order_state_code)
        .put("order_type", order_type)
    }
  }

  case class EtaData(
                      eta: Option[Long] = None,
                      distance: Option[Int] = None) extends ConnectMessage {
    override val schema: Schema = EtaDataSchema

    override def struct: Struct = {
      new Struct(schema)
        .putOptional("eta", eta)
        .putOptional("distance", distance)
    }
  }

  object EtaData {
    def create(eta: Option[Long], distance: Option[Int]): Option[EtaData] = {
      (eta.isDefined, distance.isDefined) match {
        case (false, false) => None
        case (_, _) => Some(EtaData(eta, distance))
      }
    }
  }

  case class NavigationData(
                             dest_text: Option[String] = None,
                             eta: Option[EtaData] = None) extends ConnectMessage {
    val schema = NavigationDataSchema

    def struct = {
      new Struct(schema)
        .putOptional("dest_text", dest_text)
        .putOptionalStruct("eta", eta)
    }
  }

  case class VehicleData(
                          speed: Option[Int] = None,
                          odometer: Option[Int] = None) extends ConnectMessage {
    val schema = VehicleDataSchema

    def struct = {
      new Struct(schema)
        .putOptional("speed", speed)
        .putOptional("odometer", odometer)
    }
  }

  object VehicleData {
    def create(speed: Option[Int], odometer: Option[Int]): Option[VehicleData] = {
      (odometer.isDefined, speed.isDefined) match {
        case (false, false) => None
        case (_, _) => Some(new VehicleData(speed, odometer))
      }
    }
  }

  case class WebfleetMessage(
                              msg_time: Long,
                              msg_time_str: String,
                              msgid: Long,
                              msg_class: String,
                              msg_type: String,
                              msg_type_code: Int,
                              vehicle_data: Option[VehicleData] = None,
                              msg_text: Option[String] = None,
                              object_no: String,
                              order_data: Option[OrderData] = None,
                              order_eta_data: Option[EtaData] = None,
                              navigation_data: Option[NavigationData] = None,
                              position_data: Option[PositionData] = None,
                              surplus_data: Option[Map[String, String]] = None,
                              objectuid: Option[String] = None,
                              unmatched_data: Option[Map[String, String]] = None) {

    val schema = WebfleetSchema

    import scala.collection.JavaConverters._

    def struct = {
      new Struct(schema)
        .put("msg_time", msg_time)
        .put("msg_time_str", msg_time_str)
        .put("msgid", msgid)
        .put("msg_class", msg_class)
        .put("msg_type", msg_type)
        .put("msg_type_code", new Integer(msg_type_code))
        .putOptionalStruct("vehicle_data", vehicle_data)
        .putOptional("msg_text", msg_text)
        .put("object_no", object_no)
        .putOptionalStruct("order_data", order_data)
        .putOptionalStruct("order_eta_data", order_eta_data)
        .putOptionalStruct("navigation_data", navigation_data)
        .putOptionalStruct("position_data", position_data)
        .putOptional("surplus_data", surplus_data.map(_.asJava))
        .putOptional("objectuid", objectuid)
        .putOptional("unmatched_data", unmatched_data.map(_.asJava))
    }
  }

}
