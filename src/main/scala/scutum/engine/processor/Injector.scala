package scutum.engine.processor

import akka.actor._
import java.io.File
import com.google.inject._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.ScalaModule
import scutum.engine.contracts.ProcessingService
import scutum.engine.contracts.external.ScanEvent

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
  @Singleton def geProcessingActor(implicit @Inject system: ActorSystem) = ActorMaterializer()

}


object Injector {
  case object Interrupt
  case class ProcessorConfig(jarFolder: String)
  case class ProcessingLoopArgs(service: ProcessingService, config: ProcessorConfig)


  class ActorProcessingLoop(args: ProcessingLoopArgs) extends Actor with LazyLogging{
    private var interruptLoop = false

    override def receive: Receive = {
      case Interrupt => interruptLoop = true
      case x: Any => logger.error(s"unknown message type ${x.getClass}")
    }


    private def startLoop() = {
      while(!interruptLoop) {
        val events = args.service.loadScanEvents()
        processEvents(events)
      }
    }

    private def processEvents(events: Seq[ScanEvent]) = {
      val processors = args.service.loadProcessors("")
      events.foreach(i => args.service.process(processors, i))
    }
  }
}