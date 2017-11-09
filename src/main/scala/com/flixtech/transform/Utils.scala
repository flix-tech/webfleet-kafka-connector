package com.flixtech.transform

import com.fasterxml.jackson.databind.ObjectMapper
import scala.collection.JavaConverters._

/**
  * Split a json array into its components
  */
object Utils {
  val mapper = new ObjectMapper()
  def splitJsonArray(input: String): List[String] = mapper
    .readTree(input)
    .elements()
    .asScala
    .map(_.toString)
    .toList
}
