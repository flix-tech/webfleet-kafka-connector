package com.flixtech

package object kafka {
  val webfleetSourceVersion = "v0.13"

  object ConfigKeys{
    val TOPIC = "topic"
    val ENV = "env"
    val WEBFLEET_API_ENDPOINT_URL = "webfleet.api.endpoint_url"
    val WEBFLEET_API_ACCOUNT = "webfleet.api.account"
    val WEBFLEET_API_USER = "webfleet.api.user"
    val WEBFLEET_API_PASSWORD = "webfleet.api.password"
    val WEBFLEET_API_PASSWORD_KEY = "webfleet.api.password.credstash_key"
    val WEBFLEET_API_PASSWORD_TABLE = "webfleet.api.password.credstash_table"
    val METRICS_CLASS_NAME = "metrics.class.name"
  }
}
