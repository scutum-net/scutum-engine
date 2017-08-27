package scutum.engine.repositories

import java.util.concurrent.Executors

import wabisabi.Client
import com.google.gson._

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scutum.engine.contracts.Alert
import scutum.engine.repositories.ElasticsAlertsRepository.Config

import scala.concurrent.{Await, ExecutionContext}

// elastic search alerts repo
class ElasticsAlertsRepository(config: Config) {
  val client = new Client(config.url)
  val serializer: Gson = new GsonBuilder().create()
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  def writeAlert(category: String, alert: Alert) = {
    val f = client.verifyIndex(category).map(_.getStatusCode)
    val result = Await.result(f, config.msTimeout milli)
    if(result != 200) client.createIndex(category)

    client.index(
      index = category, `type` = "alert",
      id = Some(alert.id.toString),
      data = serializer.toJson(alert),
      refresh = true
    )
  }

  def readAlert(category: String, id: Long): Try[Alert] = {
    val response = client.get(category, "alert", id.toString).map(_.getResponseBody)
    val json = Await.result(response, config.msTimeout milli)
    Try(serializer.fromJson(json, classOf[Alert]))
  }

}


object ElasticsAlertsRepository {
  case class Config(url: String, msTimeout: Int)
}