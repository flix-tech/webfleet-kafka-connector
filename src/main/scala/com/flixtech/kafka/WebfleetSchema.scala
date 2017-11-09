package com.flixtech.kafka

import org.apache.kafka.connect.data.{Schema, SchemaBuilder, Struct}

object WebfleetSchema {

  case class SchemaStruct(schema: Schema, struct: Struct)

  private def LongSchema(): SchemaBuilder = SchemaBuilder.int64()

  private def DoubleSchema() = SchemaBuilder.float64()

  private def StringSchema() = SchemaBuilder.string()

  private def IntSchema() = SchemaBuilder.int32()

  private def MapSchema() = SchemaBuilder.map(StringSchema().build(), StringSchema().build())

  private val GpsStatusField = StringSchema().doc("Possible values: UNKNOWN, LAST_KNOWN, NO_FIX, GOOD").build()
  private val PosTimeSchema = LongSchema().optional().build()
  private val PosLatitudeSchema = DoubleSchema().optional().build()
  private val PosLongitudeSchema = DoubleSchema().optional().build()
  private val PosCountrySchema = StringSchema().optional().build()
  private val PosPostcodeSchema = StringSchema().optional().build()
  private val PosAddrnoSchema = IntSchema().optional().build()
  private val PosParamsSchema = StringSchema().optional().build()
  private val PosTextSchema = StringSchema().optional().build()
  private val CourseSchema = IntSchema().optional().build()
  private val DirectionSchema = IntSchema().optional().build()

  val PositionDataSchema: Schema = SchemaBuilder
    .struct().name("com.flixfabrix.bustracking.webfleet.PositionData")
    .field("gps_status", GpsStatusField)
    .field("pos_time", PosTimeSchema)
    .field("pos_latitude", PosLatitudeSchema)
    .field("pos_longitude", PosLongitudeSchema)
    .field("pos_country", PosCountrySchema)
    .field("pos_postcode", PosPostcodeSchema)
    .field("pos_addrno", PosAddrnoSchema)
    .field("pos_params", PosParamsSchema)
    .field("pos_text", PosTextSchema)
    .field("course", CourseSchema)
    .field("direction", DirectionSchema)
    .optional().build()

  val EtaSchema: Schema = LongSchema().optional().build()
  val DistanceSchema: Schema = IntSchema().optional().build()

  val EtaDataSchema: Schema = SchemaBuilder
    .struct().name("com.flixfabrix.bustracking.webfleet.EtaData")
    .field("eta", EtaSchema)
    .field("distance", DistanceSchema)
    .doc("ETA data for msg_type 761 order-eta")
    .optional().build()

  val DestTextSchema: Schema = StringSchema().optional().build()
  val EtaNavigationSchema: Schema = EtaDataSchema

  val NavigationDataSchema: Schema = SchemaBuilder
    .struct().name("com.flixfabrix.bustracking.webfleet.NavigationData")
    .field("dest_text", DestTextSchema)
    .field("eta", EtaNavigationSchema)
    .optional().build()

  private val OrderNoSchema = StringSchema().optional().build()
  private val OrderStateSchema = StringSchema().doc("Possible values: UNKNOWN, NOT_YET_SENT, SENT, RECEIVED, READ, ACCEPTED, DELIVERY_ORDER_STARTED, RESUMED, SUSPENDED, CANCELLED, REJECTED, FINISHED").build()
  private val OrderStateCode = IntSchema().optional().build()
  private val OrderTypeSchema = StringSchema().doc("Possible values: UNKNOWN, SERVICE, PICKUP, DELIVERY").build()

  val OrderDataSchema: Schema = SchemaBuilder
    .struct().name("com.flixfabrix.bustracking.webfleet.OrderData")
    .field("orderno", OrderNoSchema)
    .field("order_state", OrderStateSchema)
    .field("order_state_code", OrderStateCode)
    .field("order_type", OrderTypeSchema)
    .optional().build()

  private val SpeedSchema = IntSchema().optional().build()
  private val OdometerSchema = IntSchema().optional().build()

  val VehicleDataSchema: Schema = {
    SchemaBuilder
      .struct().name("com.flixfabrix.bustracking.webfleet.VehicleData")
      .field("speed", SpeedSchema)
      .field("odometer", OdometerSchema)
      .optional().build()
  }

  private val MsgTimeSchema = LongSchema().doc("The message creation timestamp.").build()
  private val MsgTimeStrSchema = StringSchema().doc("The message creation timestamp.").build()
  private val MsgIdSchema = LongSchema().build()
  private val MsgClassSchema = StringSchema().doc("Possible values: UNKNOWN, SYSTEM, TEXT, POLLING, TIMER, GPS, INPUT, OUTPUT, DATA, CONFIGURATION, TRIP, ORDER").build()
  private val MsgTypeSchema = StringSchema().doc("Possible values: UNKNOWN, TRACKING, SENT, DELETED, DELETED_ALL, DESTINATION_REACHED, NAVIGATION_STARTED, NAVIGATION_CANCELLED, MESSAGE_RECEIVED, STATE, ETA, NAVIGATION, DRIVER_LOGON, DRIVER_LOGOFF, WORK_START, WORK_END, BREAK_START, BREAK_END").build()
  private val MsgTypeCodeSchema = IntSchema().build()
  private val WebfleetVehicleDataSchema = VehicleDataSchema
  private val MsgTextSchema = StringSchema().optional().doc("The webfleet message class.").build()
  private val ObjectNoSchema = StringSchema().doc("Webfleet identifier of the bus.").build()

  val OrderEtaDataSchema: Schema = EtaDataSchema
  val SurplusDataSchema: Schema = MapSchema().optional().build()
  val ObjectuidSchema: Schema = StringSchema().optional().build()
  val UnmatchedDataSchema: Schema = MapSchema().optional().build()

  val WebfleetSchema: Schema = SchemaBuilder
    .struct()
    .name("com.flixfabrix.bustracking.webfleet.WebfleetMessage")
    .field("msg_time", MsgTimeSchema)
    .field("msg_time_str", MsgTimeStrSchema)
    .field("msgid", MsgIdSchema)
    .field("msg_class", MsgClassSchema)
    .field("msg_type", MsgTypeSchema)
    .field("msg_type_code", MsgTypeCodeSchema)
    .field("vehicle_data", WebfleetVehicleDataSchema)
    .field("msg_text", MsgTextSchema)
    .field("object_no", ObjectNoSchema)
    .field("order_data", OrderDataSchema)
    .field("order_eta_data", OrderEtaDataSchema)
    .field("navigation_data", NavigationDataSchema)
    .field("position_data", PositionDataSchema)
    .field("surplus_data", SurplusDataSchema)
    .field("objectuid", ObjectuidSchema)
    .field("unmatched_data", UnmatchedDataSchema)
    .build()

}
