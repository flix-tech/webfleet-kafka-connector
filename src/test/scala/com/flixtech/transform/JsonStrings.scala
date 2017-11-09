package com.flixtech.transform

import scala.io.{Codec, Source}

/**
  * Created by francescoditrani on 09/09/17.
  */
object JsonStrings {
  def readJson(path: String): String  = {
    implicit val c = Codec.UTF8
    Source.fromInputStream(getClass.getResourceAsStream(path)).mkString
  }
}
