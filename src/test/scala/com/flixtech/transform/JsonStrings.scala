package com.flixtech.transform

import scala.io.{Codec, Source}

object JsonStrings {
  def readJson(path: String): String  = {
    implicit val c = Codec.UTF8
    Source.fromInputStream(getClass.getResourceAsStream(path)).mkString
  }
}
