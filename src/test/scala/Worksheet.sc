import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import scala.collection.JavaConverters._

val path = "/Users/dstatsen/Documents/scutum/scutum-engine/target/scala-2.12/scutum-engine-assembly-1.0.jar"


val file = new File(path)
val loader = URLClassLoader.newInstance(Array(file.toURI.toURL))

val jarFile = new JarFile(path)
val e = jarFile.entries.asScala.filter(_.getName.endsWith("class"))
val x = e.filter(_.getName.contains("Alert")).toSeq.head
x.
  val className = x.getName.replace('/', '.').replace(".class", "")
val y = loader.loadClass(className)
val z = y.getConstructors()(0).newInstance("")
