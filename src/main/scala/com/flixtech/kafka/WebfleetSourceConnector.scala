package com.flixtech.kafka

import java.util

import com.flixtech.kafka.ConfigKeys._
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.source.SourceConnector

class WebfleetSourceConnector extends SourceConnector with LazyLogging {

  logger.info(s"Stat connector at v.${version()}")

  var props: util.Map[String, String] = _

  override def start(props: util.Map[String, String]) = {
    logger.info(s"Start WebfleetSourceConnector with $props")
    this.props = props
  }

  override def taskClass() = classOf[WebfleetSourceTask]

  override def version() = webfleetSourceVersion

  override def stop() = Unit

  override def taskConfigs(maxTasks: Int) = {
    val configs = new util.ArrayList[java.util.Map[String, String]]()

    configs.add(props)
    configs
  }

  private val CONFIG_DEF = new ConfigDef()
    .define(TOPIC, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The topic to publish data to")
    .define(WEBFLEET_API_ENDPOINT_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Webfleet http endpoint url")
    .define(WEBFLEET_API_USER, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Webfleet http user")
    .define(WEBFLEET_API_ACCOUNT, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Webfleet http account")

  override def config() = CONFIG_DEF
}
