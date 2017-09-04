package scutum.engine.processor

import scala.util.Try
import akka.actor.ActorSystem
import com.google.inject.Guice
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions._
import scutum.engine.contracts.ProcessingService

object Boot extends LazyLogging{
  def main(args: Array[String]): Unit = {
    logger.debug("starting scutum processing")
    val injector = Guice.createInjector(new Injector())

    implicit val system = injector.instance[ActorSystem]
    implicit val executionContext = system.dispatcher
    implicit val materializer = injector.instance[ActorMaterializer]
    val processor = injector.instance[ProcessingService]

    // TODO: replace by actor
    while (true) {
      val processed = Try(processor.process(""))
      if(processed.getOrElse(0) == 0) Thread.sleep(1000)
      logger.info(s"processed items:${processed.getOrElse(0)}")
      if(processed.isFailure) logger.error(s"failed to process ${processed.failed.get}")
    }


    sys.addShutdownHook({
      system.terminate()
      logger.info("exiting scutum processor")
    })
  }
}
