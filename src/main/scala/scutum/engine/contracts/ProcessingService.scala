package scutum.engine.contracts

import scutum.engine.contracts.external._
import com.typesafe.scalalogging.LazyLogging

trait ProcessingService extends LazyLogging{
  def loadScanEvents(): Seq[ScanEvent]

  def publishAlert(category: String, alert: Alert): Unit


  def loadProcessors(path: String): Seq[Processor] = {
    List()
  }

  def process(processors: Seq[Processor], scanEvent: ScanEvent): Unit = {
    val processor = processors.find(_.getScanType == scanEvent.scanType)
    if(processor.isEmpty){
      logger.error(s"unknown scanner type ${scanEvent.scanType}")
      publishAlert("unknown_scanner", Alert(scanEvent.scanType, scanEvent.data))
    }
    else{
      val result = processor.get.process(scanEvent)
      result.foreach(i => publishAlert(scanEvent.scanType.toString, i))
    }
  }
}
