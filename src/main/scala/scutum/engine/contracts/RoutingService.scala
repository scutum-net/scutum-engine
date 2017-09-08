package scutum.engine.contracts

import java.time._
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

trait RoutingService extends LazyLogging{
  def publishData(key: String, data: String): Unit
  def authorizeCustomer(customerId: Int): Boolean

  def routeDefault: Route = get {
    pathPrefix(""){
      complete("Scutum ingest")
    }
  }

  def routeAuth: Route = get {
    pathPrefix("auth" / IntNumber){ customerId =>
      logger.debug(s"routeAuth $customerId")

      if(authorizeCustomer(customerId)) {
        val id = RoutingService.generateSessionId()
        complete(s"$id")
      }
      else
        complete("Error")
    }
  }

  def routeEvent: Route = post {
    pathPrefix("event" / IntNumber / IntNumber / LongNumber) {
      (customerId, scannerType, sessionId) =>
        entity(as[String]) { data =>
          logger.debug(s"routeEvent $customerId, $scannerType, $sessionId")
          Try(publishData(sessionId.toString, data)) match{
            case Success(x) =>
              complete(s"done $customerId $scannerType $sessionId")
            case Failure(x) =>
              logger.error(s"routeEvent failed ${x.getMessage}")
              complete(s"failed $x")
          }
        }
    }
  }

  def getRoutes: Route = routeAuth ~ routeDefault ~ routeEvent
}


object RoutingService {
  private val epoch = LocalDateTime.of(2017, 1, 1, 0, 0, 0)

  def generateSessionId(): Long = {
    val milliseconds = epoch.until(LocalDateTime.now(ZoneOffset.UTC), ChronoUnit.NANOS) / 1000000
    milliseconds << 22
  }
}