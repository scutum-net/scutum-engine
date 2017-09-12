package scutum.engine.ingest

import java.io.File

import com.google.inject._
import akka.actor.ActorSystem
import scutum.engine.contracts._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.ScalaModule
import scutum.engine.ingest.Injector.Configuration
import scutum.engine.repositories.{FileSystemRepository, KafkaEventsRepository}


class Injector extends AbstractModule with ScalaModule with LazyLogging {
  // configure injector
  override def configure(): Unit = {
    val logFile = new File("./logback.xml")
    if (logFile.exists) System.setProperty("logback.configurationFile", logFile.getCanonicalPath)
    logger.info(s"logback loaded: ${logFile.getCanonicalPath} ${logFile.exists}")
  }

  // create actor system
  @Provides
  @Singleton def getActorSystem = ActorSystem("scutum-ingest")

  // get materializer
  @Provides
  @Singleton def getMaterializer(implicit @Inject system: ActorSystem) = ActorMaterializer()


  @Provides
  @Singleton def getConfig: Config = {
    val configFile = new File(FileSystemRepository.getRunningDirectory + "/app.conf")
    logger.info(s"config loaded: ${configFile.getCanonicalPath} ${configFile.exists}")
    if (configFile.exists) ConfigFactory.parseFile(configFile) else ConfigFactory.load("app.conf")
  }


  @Provides
  @Singleton def getCommonConfig(implicit @Inject config: Config): Configuration = {
    Configuration(
      config.getInt("conf.port"),
      config.getString("conf.host"))
  }


  @Provides
  @Singleton def getRoutingService(implicit @Inject config: Config): RoutingService = {
    val eventsRepository = KafkaEventsRepository.create(config)

    new {} with RoutingService {
      override def authorizeCustomer(customerId: Int): Boolean = customerId == 0
      override def publishData(key: String, data: String): Unit = eventsRepository.publish(key, data)
    }
  }
}

object Injector {
  case class Configuration(port: Int, host: String)
}