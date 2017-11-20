package com.flixtech.metrics

import com.codahale.metrics.{Gauge, JmxReporter, MetricRegistry}
import com.typesafe.scalalogging.LazyLogging

object BaseMetrics extends LazyLogging {

  private val metrics = new MetricRegistry()

  val AGE_OLDEST = createGauge("AGE_OLDEST")

  val AGE_YOUNGEST = createGauge("AGE_YOUNGEST")

  val DURATION = createGauge("DURATION")

  def count(name: String, n: Long = 1) = {
    metrics.counter(name).inc(n)
  }


  val reporter = JmxReporter.forRegistry(metrics).build()
  reporter.start()

  trait InternalGauge[T] extends Gauge[T] {
    def setValue(_value: T)
  }

  private def createGauge(name: String): InternalGauge[Long] = {
    val gauge = new InternalGauge[Long] {
      var value: Long = 0

      override def getValue = value

      override def setValue(_value: Long) = value = _value
    }
    metrics.register(name, gauge)
    gauge
  }

}
