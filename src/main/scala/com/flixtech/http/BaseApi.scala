package com.flixtech.http

import com.flixtech.metrics.BaseMetrics
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseApi extends LazyLogging {
  def poll: Future[Option[String]]

  def ack: Future[Unit]
}

object HttpApi extends LazyLogging {
  def apply(
             webfleetApiAccount: String,
             webfleetApiUsername: String,
             webfleetApiPassword: String,
             webfleetApiEndpointUrl: String
           ) = {

    new HttpApi(webfleetApiEndpointUrl, webfleetApiAccount, webfleetApiUsername, webfleetApiPassword, HttpTransport)
  }
}

class HttpApi(
               url: String,
               account: String,
               user: String,
               password: String,
               transport: Transport) extends BaseApi {

  override def poll: Future[Option[String]] = {
    val future = transport.get(url, paramsWith(action = "popQueueMessagesExtern")).map { response =>
      response.status match {
        case 200 =>
          BaseMetrics.count(name = response.status.toString)
          Some(response.body)
        case status =>
          logger.error(s"Status: $status when calling popQueueMessagesExtern")
          None
      }

    }

    future.failed.foreach {
      case ex =>
        logger.error(s"error while fetching webfleet: $ex")
        BaseMetrics.count(name = "ERROR")
    }

    future
  }

  def close = transport.close

  override def ack: Future[Unit] = {
    transport.get(url, paramsWith(action = "ackQueueMessagesExtern")).map { response =>
      BaseMetrics.count(name = response.status.toString)
    }.recover {
      case ex =>
        logger.error(s"error while ack: $ex")
        BaseMetrics.count(name = "ACK_ERROR")
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
