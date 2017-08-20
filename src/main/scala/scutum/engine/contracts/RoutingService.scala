package scutum.engine.contracts

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging

trait RoutingService extends LazyLogging{
  def publishData(data: String): Unit
  def authorizeCustomer(customerId: Int): Boolean

  def routeDefault: Route = get {
    pathPrefix(""){
      complete("Scutum ingest")
    }
  }

  def routeAuth: Route = get {
    pathPrefix("auth" / IntNumber){ customerId =>
      if(authorizeCustomer(customerId))
        complete(s"customer $customerId")
      else
        complete("Error")
    }
  }

  def getRoutes: Route = routeAuth ~ routeDefault
}
