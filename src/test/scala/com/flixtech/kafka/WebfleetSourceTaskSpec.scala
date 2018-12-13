package com.flixtech.kafka

import java.util.Collections

import com.flixtech.Message.WebfleetMessage
import com.flixtech.http.BaseApi
import com.flixtech.kafka.ConfigKeys._
import com.flixtech.kafka.WebfleetSourceTask.ParseResponse
import org.apache.kafka.connect.source.SourceRecord
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{verify, _}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.Future

class WebfleetSourceTaskSpec extends FlatSpec with Matchers with MockitoSugar {

  trait Context {
    val msgA = WebfleetMessage(msg_time = 200, "now", 123, "CONFIGURATION", "BREAK_END", 1, None, None, "msgA", None, None, None, None, None, None, None)
    val msgB = msgA.copy(msg_time = 250, object_no = "msgB")
    val webfleetMessages = List(msgA, msgB)

    val parser = mock[(String) => ParseResponse]
    when(parser.apply("http response")).thenReturn(ParseResponse(webfleetMessages.size, webfleetMessages))

    def throttle() = Unit

    val mockedNow = mock[Function0[Long]]
    when(mockedNow()).thenReturn(100l, 200l, 300l)

    val api = mock[BaseApi]
    when(api.poll).thenReturn(Future.successful(Some("http response")))

    val getApi = mock[(String, String, String, String) => BaseApi]
    when(getApi.apply(ArgumentMatchers.eq("my_account"), ArgumentMatchers.eq("my_user"), ArgumentMatchers.eq("my_password"), ArgumentMatchers.eq("http://endpoint"))).thenReturn(api)


    val task = new WebfleetSourceTask(getApi, parser, throttle, mockedNow)

    val config: java.util.Map[String, String] = new java.util.HashMap[String, String]()
    config.put(ConfigKeys.TOPIC, "test_topic")
    config.put(WEBFLEET_API_PASSWORD_TABLE, "my_table")
    config.put(WEBFLEET_API_ACCOUNT, "my_account")
    config.put(WEBFLEET_API_USER, "my_user")
    config.put(WEBFLEET_API_PASSWORD, "my_password")
    config.put(WEBFLEET_API_ENDPOINT_URL, "http://endpoint")
    task.start(config)
  }

  "Webfleet Task" should "forward commit to api ack when task is stopped" in new Context {
    task.stop()
    verify(api).ack
  }


  it should "fetch http, parse and return" in new Context {
    val result = task.poll()
    verify(api).poll

    val sourcePartition = Collections.singletonMap("endpoint", "webfleet")

    def sourceOffset(id: Int) = Collections.singletonMap("position", "offset")

    val (keySchemaA, keyValueA) = WebfleetSourceTask.getKeySchemaValue("msgA")
    val (keySchemaB, keyValueB) = WebfleetSourceTask.getKeySchemaValue("msgB")

    val output = List(
      new SourceRecord(sourcePartition, sourceOffset(0), "test_topic", null, keySchemaA, keyValueA, msgA.schema, msgA.struct, 200.toLong),
      new SourceRecord(sourcePartition, sourceOffset(1), "test_topic", null, keySchemaB, keyValueB, msgB.schema, msgB.struct, 250.toLong)).asJava

    output should be(result)

    task.poll()

    verify(api).ack
    verify(api, times(2)).poll

  }

  it should "return List.empty when api.poll() return None" in new Context {
    when(api.poll).thenReturn(Future.successful(None))

    val result = task.poll()
    verify(api).poll

    List.empty[SourceRecord].asJava should be(result)
  }
}
