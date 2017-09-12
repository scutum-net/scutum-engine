package scutum.engine.repositories

import java.io.File
import scala.collection.JavaConverters._

object FileSystemRepository {

  def loadFiles(path: String, extension: String): Seq[String] = {
    val files = new File(path).listFiles().toList.asJava.asScala
    files.map(_.getCanonicalPath).filter(_.endsWith(extension))
  }

  def loadFile(path: String, fileName: String): Option[String] = {
    val file = new File(s"$path/$fileName")
    if(file.exists) Some(file.getCanonicalPath) else None
  }

  def getRunningDirectory: String = {
    val codeSource = this.getClass.getProtectionDomain.getCodeSource
    new File(codeSource.getLocation.toURI.getPath).getParentFile.getPath
  }
}
