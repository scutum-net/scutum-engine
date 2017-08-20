package scutum.engine.ingest

import java.io.File
import com.google.inject._
import akka.actor.ActorSystem
import scutum.engine.contracts._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.ScalaModule


class Injector extends AbstractModule with ScalaModule with LazyLogging {
  override def configure(): Unit = {
    val logFile = new File("./logback.xml")
    if (logFile.exists) System.setProperty("logback.configurationFile", logFile.getCanonicalPath)
    logger.info(s"logback loaded: ${logFile.getCanonicalPath} ${logFile.exists}")
  }

  @Provides
  @Singleton def getActorSystem = ActorSystem("scutum-ingest")

  @Provides
  @Singleton def getMaterializer(implicit @Inject system: ActorSystem) = ActorMaterializer()

  @Provides
  @Singleton def getConfig: Configuration = {
    val logFile = new File("./app.conf")
    logger.info(s"config loaded: ${logFile.getCanonicalPath} ${logFile.exists}")
    Configuration.parseConfig(if (logFile.exists)
      ConfigFactory.parseFile(logFile) else ConfigFactory.load("app.conf"))
  }

  @Provides
  @Singleton def getRoutingService: RoutingService = {
    new {} with RoutingService {
      override def authorizeCustomer(customerId: Int): Boolean = true
      override def publishData(data: String): Unit = logger.debug("publish")
    }
  }

}
