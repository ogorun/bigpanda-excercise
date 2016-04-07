package com.bigpanda.http.spray

import com.typesafe.config.Config

/**
  * Created by olgagorun on 08/04/2016.
  */
trait Configured {
  def config: Config
}
