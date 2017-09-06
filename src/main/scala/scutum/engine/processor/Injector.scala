package scutum.engine.processor

import akka.actor._
import java.io.File

import com.google.inject._
import com.typesafe.config._
import scutum.engine.repositories._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.ScalaModule
import scutum.core.contracts.{Alert, ScannedData}
import scutum.engine.contracts.ProcessingService

class Injector extends AbstractModule with ScalaModule with LazyLogging {

  override def configure(): Unit = {
    val logFile = new File("./logback.xml")
    if (logFile.exists) System.setProperty("logback.configurationFile", logFile.getCanonicalPath)
    logger.info(s"logback loaded: ${logFile.getCanonicalPath} ${logFile.exists}")
  }

  @Provides
  @Singleton def getActorSystem = ActorSystem("scutum-processor")

  @Provides
  @Singleton def getMaterializer(implicit @Inject system: ActorSystem) = ActorMaterializer()

  @Provides
  @Singleton def getConfig: Config = {
    val configFile = new File("./app.conf")
    logger.info(s"config loaded: ${configFile.getCanonicalPath} ${configFile.exists}")
    if (configFile.exists)
      ConfigFactory.parseFile(configFile)
    else
      ConfigFactory.load("app.conf")
  }

  @Provides
  @Singleton def getProcessingService(implicit @Inject config: Config): ProcessingService = {
    val kafka = KafkaEventsRepository.create(config)
    val elasticsearch = ElasticsAlertsRepository.create(config)

    new {} with ProcessingService {
      override def loadScanEvents(): Seq[ScannedData] = kafka.consume()
      override def publishAlert(category: String, alert: Alert): Unit = elasticsearch.create(category, alert)
    }
  }
}
