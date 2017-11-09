package com.flixtech.metrics

import java.util

import com.typesafe.scalalogging.LazyLogging

class BaseMetrics extends LazyLogging {
  val namespace = "webfleet/connectSource"

  def start(props: util.Map[String, String]) = {}

  def metric(metricName: String, value: Double, unit: String, dimensions: List[DimensionItem] = Nil): Unit = {}

  def count(metricName: String, value: Double = 1d, dimensions: List[DimensionItem] = Nil): Unit = {}

  def millis(metricName: String, value: Double, dimensions: List[DimensionItem] = Nil): Unit = {}

}

case class DimensionItem(name: String, value: String)
