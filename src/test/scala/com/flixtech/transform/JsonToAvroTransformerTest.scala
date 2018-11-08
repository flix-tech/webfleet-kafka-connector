package com.flixtech.transform

import com.flixtech.Message._
import com.flixtech.transform.JsonStrings.readJson
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

class JsonToAvroTransformerTest extends WordSpec with ScalaFutures with MockitoSugar {

  "JsonToAvroTransformer" should {

    "construct vehicle data" in {

      val vehicleData = Some(
        VehicleData(
          speed = Some(50),
          odometer = Some(60)
        )
      )

      val orderData = Some(
        OrderData(
          orderno = Some("order no"),
          order_state = "my sate",
          order_state_code = Some(3),
          order_type = "type of order"
        )
      )

      val orderEtaData = Some(
       EtaData(
         eta = Some(1122),
         distance = Some(1144)
       )
      )

      val navData = Some(
        NavigationData(
          dest_text = Some("testText")
        )
      )

      val posData = Some(
        PositionData(
          gpsStatus = "22",
          pos_text = Some("time text"),
          pos_time = Some(334455),
          pos_latitude = Some(44),
          pos_longitude = Some(55),
          pos_country = Some("de"),
          pos_postcode = Some("112233"),
          pos_addrno = Some(5),
          pos_params = Some("params"),
          course = Some(6),
          direction = Some(9)
        )
      )

      val item = WebfleetMessage(
        msg_time = 123123,
        msg_time_str = "now",
        msgid = 123,
        msg_class = "my_class",
        msg_type = "type of message",
        msg_type_code = 1,
        vehicle_data = vehicleData,
        msg_text = Some("my msg text"),
        object_no = "object no",
        order_data = orderData,
        order_eta_data = orderEtaData,
        navigation_data = navData,
        position_data = posData,
        surplus_data = Some(Map("keyA" -> "valB")),
        objectuid = Some("object uid"),
        unmatched_data = Some(Map( "key" -> "value"))
      )

    }

    "parse with surplus data" in {
      val message = JsonToAvroTransformer.jsonToMessage(readJson("/order_messages/surplus_data.json")).get
    }

    "parse common fields" in {
      val message = JsonToAvroTransformer.jsonToMessage(readJson("/order_messages/order_state.json")).get
      assert("ORDER" === message.msg_class)
      assert("STATE" === message.msg_type)
      assert("002" === message.object_no)
      assert("1-1-299CF855B" === message.objectuid.get)
      assert(760 === message.msg_type_code)
      assert("Auftrag N06-03,15.05/002#6: begonnen" === message.msg_text.get)
      assert(1494811907000L === message.msg_time)
      assert("002" === message.object_no)
    }

    "parse order state" in {
      val message = JsonToAvroTransformer.jsonToMessage(readJson("/order_messages/order_state.json")).get
      assert("STATE" === message.msg_type)
      assert(760 === message.msg_type_code.intValue)
      assert("N06-03,15.05/002#6" === message.order_data.get.orderno.get)
      assert("DELIVERY" === message.order_data.get.order_type)
      assert("DELIVERY_ORDER_STARTED" === message.order_data.get.order_state)
    }

    "parse order eta" in {
      val message = JsonToAvroTransformer.jsonToMessage(readJson("/order_messages/order_eta.json")).get
      assert(36152 === message.order_eta_data.get.distance.get)
      assert(1494946033000L === message.order_eta_data.get.eta.get)
    }

    "parse tracking data" in {
      val message = JsonToAvroTransformer.jsonToMessage(readJson("/order_messages/timer_tracking.json")).get
      assert("GOOD" === message.position_data.get.gpsStatus)
      assert(1497959525000L === message.position_data.get.pos_time.get)
      assert(51.363008 === message.position_data.get.pos_latitude.get, 0.0000001)
      assert(13.738070 === message.position_data.get.pos_longitude.get, 0.0000001)
      assert(225 === message.position_data.get.course.get)
      assert(6 === message.position_data.get.direction.get)
      assert("DE" === message.position_data.get.pos_country.get)
      assert("zip=01990;country=DE;town1_name=Dresden;distance=34670;town1_country=DE;street=A13/E55;location_type=64;direction=N;" === message.position_data.get.pos_params.get)
      assert("35 km N von Dresden,A13/E55 (DE 01990)" === message.position_data.get.pos_text.get)
      assert(message.position_data.get.pos_addrno === None)
    }

    "parse data navigation started" in {
      val message = JsonToAvroTransformer.jsonToMessage(readJson("/order_messages/data_navigation_started.json")).get
      assert("DATA" === message.msg_class)
      assert("NAVIGATION_STARTED" === message.msg_type)
    }

    "parse data without order_type" in {
      val json = readJson("/order_messages/deletion_wo_order_type.json")
      val message = JsonToAvroTransformer.jsonToMessage(json).get
      assert("UNKNOWN" === message.order_data.get.order_type)
    }

    "parse data without order_state" in {
      val json = readJson("/order_messages/deletion_wo_order_state.json")
      val message = JsonToAvroTransformer.jsonToMessage(json).get
      assert("UNKNOWN" === message.order_data.get.order_state)
    }
  }
}
