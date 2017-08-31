package scutum.engine.ingest

import akka.http.scaladsl._
import akka.actor.ActorSystem
import com.google.inject.Guice
import scutum.engine.contracts._
import com.typesafe.scalalogging._
import akka.stream.ActorMaterializer
import scutum.engine.ingest.Injector.Configuration
import net.codingwell.scalaguice.InjectorExtensions._


object Boot extends LazyLogging{
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(new Injector())
    val config = injector.instance[Configuration]
    val routes = injector.instance[RoutingService]

    implicit val system = injector.instance[ActorSystem]
    implicit val executionContext = system.dispatcher
    implicit val materializer = injector.instance[ActorMaterializer]

    logger.debug(s"starting web server on ${config.host} ${config.port}")
    val binding = Http().bindAndHandle(routes.getRoutes, config.host, config.port)


    sys addShutdownHook {
      logger.debug(s"sessions-finalizer service is terminating.")
      binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
    }
  }
}
