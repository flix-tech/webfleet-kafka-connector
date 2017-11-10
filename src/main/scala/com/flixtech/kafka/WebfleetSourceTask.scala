package com.flixtech.kafka

import java.util
import java.util.Collections

import com.flixtech.Message.WebfleetMessage
import com.flixtech.http.{BaseApi, HttpApi}
import com.flixtech.kafka.ConfigKeys._
import com.flixtech.kafka.WebfleetSourceTask.ParseResponse
import com.flixtech.metrics.{BaseMetrics, _}
import com.flixtech.transform.{JsonToAvroTransformer, Utils}
import com.jessecoyle.JCredStash
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.connect.data.{SchemaBuilder, Struct}
import org.apache.kafka.connect.source.{SourceRecord, SourceTask}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object WebfleetSourceTask extends LazyLogging {

  case class ParseResponse(webfleetResponseCount: Int, validParsed: List[WebfleetMessage])

  def parser(input: String) = {
    val split = Utils.splitJsonArray(input)
    val validParsed = split.flatMap { jsonString =>
      JsonToAvroTransformer.jsonToMessage(jsonString) match {
        case Success(item) => Some(item)
        case Failure(err) =>
          logger.error(s"Failed to parse json '$jsonString': Ex: ${err.getMessage}")
          None
      }
    }
    ParseResponse(split.size, validParsed)
  }

  def throttle() = {
    logger.info(s"poll $webfleetSourceVersion at ${System.currentTimeMillis()}")
    logger.info(s"sleep 6 seconds to not overload webfleet api")
    // Webfleet only allows 10 calls per minute.
    Thread.sleep(6000)
  }

  def getKeySchemaValue(msgId: String) = {
    val keySchema = SchemaBuilder.struct()
      .name("webfleetSchema")
      .field("object_no", SchemaBuilder.string().build())
      .build()
    val keyValue = new Struct(keySchema).put("object_no", msgId)
    (keySchema, keyValue)
  }

  val now = () => System.currentTimeMillis()

}

class WebfleetSourceTask(
                          getHttpApi: (String, String, String, String) => BaseApi,
                          parser: (String) => ParseResponse,
                          throttle: () => Unit,
                          now: () => Long) extends SourceTask with LazyLogging {

  /**
    * required by the connect framework to instantiate this connector
    */
  def this() {
    this(
      HttpApi.apply,
      WebfleetSourceTask.parser,
      WebfleetSourceTask.throttle,
      WebfleetSourceTask.now)
  }

  var api: BaseApi = _
  var topic: String = _
  var webfleetEndpointUrl: String = _
  var webfleetApiAccount: String = _
  var webfleetApiUser: String = _
  var webfleetApiPassword: String = _

  override def start(props: util.Map[String, String]) = {
    logger.info(s"Start WebfleetSourceTask with $props")

    topic = props.get(TOPIC)
    webfleetEndpointUrl = props.get(WEBFLEET_API_ENDPOINT_URL)
    webfleetApiAccount = props.get(WEBFLEET_API_ACCOUNT)
    webfleetApiUser = props.get(WEBFLEET_API_USER)

    webfleetApiPassword = getPassword(props)


    api = getHttpApi(webfleetApiAccount, webfleetApiUser, webfleetApiPassword, webfleetEndpointUrl)
  }

  override def version() = webfleetSourceVersion

  var polled = false

  override def stop() = {
    doAck
    logger.info(s"stop tasks ${version()}")
  }

  private def doAck = {
    logger.info(s"start commit")
    api.ack
    logger.info(s"finished commit")
  }

  /**
    * called by kafka connect
    */
  override def poll(): util.List[SourceRecord] = {
    if (polled) doAck

    throttle()

    val start = now()

    def validHttpFetch(httpBody: String) = {
      val duration = now() - start

      val ParseResponse(responseCount, webfleetMessages) = parser(httpBody)

      BaseMetrics.count("INPUT_COUNT", responseCount)

      val output = webfleetMessages.map { webfleetMessage =>

        val (keySchema, keyValue) = WebfleetSourceTask.getKeySchemaValue(webfleetMessage.object_no)
        val sourcePartition = Collections.singletonMap("endpoint", "webfleet")
        val sourceOffset = Collections.singletonMap("position", "offset")

        val msg = new SourceRecord(
          sourcePartition,
          sourceOffset,
          topic,
          null,
          keySchema,
          keyValue,
          webfleetMessage.schema,
          webfleetMessage.struct,
          webfleetMessage.msg_time)

        (msg, webfleetMessage.msg_time)
      }

      BaseMetrics.DURATION.setValue(duration)

      val pollItems = output.size
      if (pollItems > 0) {
        val oldest = now() - output.map(_._2).min
        val youngest = now() - output.map(_._2).max
        BaseMetrics.AGE_OLDEST.setValue(oldest)
        BaseMetrics.AGE_YOUNGEST.setValue(youngest)
      }
      BaseMetrics.count("OUTPUT_COUNT", pollItems)

      /**
        * we can wait for the Connect framework to commit - we need to do it right before we pull the next time
        */
      polled = true
      output.map(_._1).asJava
    }

    Try(Await.result(api.poll, 20 seconds)) match {
      case Success(httpBody) => validHttpFetch(httpBody)
      case Failure(ex) =>
        logger.error(s"Error while fetching: ${ex.getMessage}")
        List.empty.asJava
    }
  }

  private def getPassword(props: util.Map[String, String]): String = {
    props.get(WEBFLEET_API_PASSWORD) match {
      case null => getPasswordCredStash(props)
      case pass => pass
    }
  }

  private def getPasswordCredStash(props: util.Map[String, String]): String = {
    val webfleetApiPasswordKey = props.get(WEBFLEET_API_PASSWORD_KEY)
    val webfleetApiPasswordTable = props.get(WEBFLEET_API_PASSWORD_TABLE)
    val cs = if (webfleetApiPasswordTable.nonEmpty) new JCredStash(webfleetApiPasswordTable) else new JCredStash()

    logger.info(s"get password for key '$webfleetApiPasswordKey' and table '$webfleetApiPasswordTable")

    cs.getSecret(webfleetApiPasswordKey, new java.util.HashMap[String, String]())
  }

}
