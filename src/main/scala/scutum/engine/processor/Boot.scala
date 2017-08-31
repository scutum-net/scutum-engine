package scutum.engine.processor

import akka.actor._
import akka.actor.ActorSystem
import com.google.inject.Guice
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions._

class Boot extends LazyLogging{
  def main(args: Array[String]): Unit = {
    logger.debug("starting scutum processing")
    val injector = Guice.createInjector(new Injector())

    implicit val system = injector.instance[ActorSystem]
    implicit val executionContext = system.dispatcher
    implicit val materializer = injector.instance[ActorMaterializer]

    // start processing loop
    val processingLoop = injector.instance[ActorRef]

    sys.addShutdownHook({
      processingLoop ! Injector.Interrupt
      system.terminate()
      logger.info("exiting scutum processor")
    })
  }
}
