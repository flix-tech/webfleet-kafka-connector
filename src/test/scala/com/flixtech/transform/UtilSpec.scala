package com.flixtech.transform

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class UtilSpec extends WordSpec with Matchers with MockitoSugar {

  "Json Utils" should {
    "split input json array" in {
      val jsonA = """{"someKey":44,"anotherKey":"another Value","item":{"input":123}}"""
      val jsonB = """{"someKey":55,"anotherKey":"another Value","item":{"input":123}}"""
      val input = s"[$jsonA, $jsonB]"
      val parsed = Utils.splitJsonArray(input)

      parsed should contain(jsonA)
      parsed should contain(jsonB)
    }
  }
}
