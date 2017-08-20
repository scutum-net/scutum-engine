package scutum.engine.contracts

import com.typesafe.config.Config

object ConfigurationParser{
  // configuration case class
  case class Configuration (port: Int, host: String)

  // parse configuration file
  def parseConfig(config: Config): Configuration = {
    Configuration(config.getInt("conf.port"), config.getString("conf.host"))
  }
}
