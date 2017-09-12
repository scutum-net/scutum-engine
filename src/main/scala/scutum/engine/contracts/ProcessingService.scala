package scutum.engine.contracts

import scutum.core.contracts._
import com.typesafe.scalalogging.LazyLogging
import scutum.engine.repositories.FileSystemRepository

trait ProcessingService extends LazyLogging {
  def loadScanEvents(): Seq[ScannedData]

  def publishAlert(category: String, alert: Alert): Unit


  def loadProcessors(path: String): Seq[Processor] = {
    val jars =  FileSystemRepository.loadFiles(path, "jar")
    List()
  }

  def process(path: String): Int = {
    val scanEvents = loadScanEvents()
    val processors = loadProcessors(path)
    scanEvents.foreach(i => process(processors, i))
    scanEvents.length
  }

  def process(processors: Seq[Processor], scanEvent: ScannedData): Unit = {
    val processor = processors.find(_.getProviderId == scanEvent.getProviderId)
    if (processor.isEmpty) {
      logger.error(s"unknown scanner type ${scanEvent.getProviderId}")
      publishAlert("unknown_scanner", new Alert(scanEvent.getProviderId + scanEvent.getData))
    }
    else {
      val result = processor.get.process(scanEvent)
      result.foreach(i => publishAlert(scanEvent.getProviderId.toString, i))
    }
  }
}
