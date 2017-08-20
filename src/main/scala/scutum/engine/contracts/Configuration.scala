package scutum.engine.contracts

import com.typesafe.config.Config

/**
  * Created by dstatsen on 20/08/2017.
  */
case class Configuration (port: Int, host: String)


object Configuration{
  def parseConfig(config: Config): Configuration = {
    Configuration(config.getInt("conf.port"), config.getString("conf.host"))
  }
}
