package com.flixtech.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

trait Transport {
  def close: Unit

  def get(url: String, params: Map[String, String]): Future[StandaloneWSResponse]
}

object HttpTransport extends Transport {

  val conf = ConfigFactory.load()

  implicit private val system = ActorSystem("httpTransport", conf)
  implicit private val materializer = ActorMaterializer()

  private val ws = StandaloneAhcWSClient()

  def get(url: String, params: Map[String, String]) = {
    val request = ws.url(url)
    params.foldLeft(request) { (req, param) =>
      req.addQueryStringParameters(param)
    }.get()
  }

  private def shutdown = {
    ws.close()
    system.terminate()
  }

  override def close: Unit = shutdown

  sys.addShutdownHook {
    shutdown
  }

}
