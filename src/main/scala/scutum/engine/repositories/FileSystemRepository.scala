package scutum.engine.repositories

import java.io.File
import scala.collection.JavaConverters._


object FileSystemRepository {
  def loadFilesFromRunningDirectory(extension: String): Seq[String] = {
    val dir = new File(getRunningDirectory)
    val files = dir.listFiles().toList.asJava.asScala
    files.map(_.getCanonicalPath).filter(_.endsWith(extension))
  }

  def loadFileFromRunningDirectory(fileName: String): Option[String] = {
    val file = new File(s"$getRunningDirectory/$fileName")
    if(file.exists()) Some(file.getCanonicalPath) else None
  }

  def getRunningDirectory: String = {
    val codeSource = this.getClass.getProtectionDomain.getCodeSource
    new File(codeSource.getLocation.toURI.getPath).getParentFile.getPath
  }
}
