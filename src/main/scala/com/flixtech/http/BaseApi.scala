package com.flixtech.http
import com.flixtech.metrics._
import com.flixtech.metrics.BaseMetrics
import com.jessecoyle.JCredStash
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseApi extends LazyLogging {
  def poll: Future[String]

  def ack: Future[Unit]
}

object HttpApi extends LazyLogging {
  def apply(
             metrics: BaseMetrics,
             webfleetApiAccount: String,
             webfleetApiUsername: String,
             webfleetApiPassword: String,
             webfleetApiEndpointUrl: String
           ) = {

    new HttpApi(webfleetApiEndpointUrl, webfleetApiAccount, webfleetApiUsername, webfleetApiPassword, HttpTransport, metrics)
  }
}

class HttpApi(
               url: String,
               account: String,
               user: String,
               password: String,
               transport: Transport,
               metrics: BaseMetrics) extends BaseApi {

  override def poll: Future[String] = {
    val future = transport.get(url, paramsWith(action = "popQueueMessagesExtern")).map { response =>
      metrics.count(name = response.status.toString)
      response.body
    }

    future.failed.foreach {
      case ex =>
        logger.error(s"error while fetching webfleet: $ex")
        metrics.count(name = "ERROR")
    }
    future
  }

  def close = transport.close

  override def ack: Future[Unit] = {
    transport.get(url, paramsWith(action = "ackQueueMessagesExtern")).map { response =>
      metrics.count(name = response.status.toString)
    }.recover {
      case ex =>
        logger.error(s"error while ack: $ex")
        metrics.count(name = "ACK_ERROR")
    }
  }

  private def paramsWith(action: String) = {
    Map(
      "lang" -> "en",
      "action" -> action,
      "msgclass" -> "0",
      "account" -> account,
      "username" -> user,
      "password" -> password,
      "outputformat" -> "json"
    )
  }
}
