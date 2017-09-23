package scutum.engine.contracts

import java.io.File
import java.net.{URL, URLClassLoader}
import java.util.jar.JarFile

import scala.collection.JavaConverters._
import scutum.core.contracts._
import com.typesafe.scalalogging.LazyLogging
import scutum.engine.repositories.FileSystemRepository

import scala.util.{Failure, Success, Try}

trait ProcessingService extends LazyLogging {
  def loadScanEvents(): Seq[ScannedData]

  def publishAlert(category: String, alert: Alert): Unit

  def loadProcessors(path: String): Seq[IProcessor] = {
    val files = FileSystemRepository.loadFiles(path, "jar")
    val processors = files.flatMap(i => loadClasses(new File(i)))
    processors
  }

  private def loadClasses(file: File) = {
    val loader = URLClassLoader.newInstance(Array(file.toURI.toURL))
    val entries = Try(new JarFile(file.getCanonicalPath).entries.asScala)

    val processors = entries.get
      .map(_.getName)
      .filter(_.endsWith("class"))
      .filter(_.contains("scutum"))
      .map(_.replace('/', '.').replace(".class", "")).toList

    processors.map(i => Try(getProcessor(loader, i))).filter(_.isSuccess).map(_.get)
  }

  private def getProcessor(loader: ClassLoader, className: String) = {
    loader.loadClass(className)
      .getConstructors()(0)
      .newInstance()
      .asInstanceOf[IProcessor]
  }

  def process(path: String): Int = {
    val scanEvents = loadScanEvents()
    val processors = loadProcessors(path)
    scanEvents.foreach(i => process(processors, i))
    scanEvents.length
  }

  def process(processors: Seq[IProcessor], scanEvent: ScannedData): Unit = {
    val processor = processors.find(_.getProviderId == scanEvent.getProviderId)
    if (processor.isEmpty) {
      logger.error(s"unknown scanner type ${scanEvent.getProviderId}")
      publishAlert("unknown_scanner", new Alert(scanEvent.getProviderId + scanEvent.getData))
    }
    else {
      Try(processor.get.process(scanEvent)) match {
        case Success(x) => x.foreach(i => publishAlert(scanEvent.getProviderId.toString, i))
        case Failure(x) => logger.error(s"processor ${processor.get.getClass.getName} failed $x")
      }
    }
  }
}
