package scutum.engine.repositories

import wabisabi.Client
import com.google.gson._

import scala.concurrent._
import java.lang.reflect._

import com.typesafe.config._

import scala.concurrent.duration._
import scutum.engine.contracts._
import java.util.concurrent.Executors

import scutum.engine.contracts.external.Alert
import scutum.engine.repositories.ElasticsAlertsRepository._

// elastic search alerts repo
class ElasticsAlertsRepository(config: ElasticSearchConfig) {
  private val client = new Client(config.url)
  private val serializer: Gson = new GsonBuilder()
    .registerTypeAdapter(Alert.getClass, new Serializer).create()
  private val threadsPool = Executors.newFixedThreadPool(config.ioThreads)
  private implicit val context = ExecutionContext.fromExecutor(threadsPool)

  def create(category: String, alert: Alert): Unit = {
    val f = client.verifyIndex(category).map(_.getStatusCode)
    val result = Await.result(f, config.msTimeout milli)
    if(result != 200) client.createIndex(category)

    val id = Some(alert.id.toString)
    val data = serializer.toJson(alert)
    client.index(category, "alert", id, data, refresh = true)
  }

  def read(category: String, id: Long): Alert = {
    val response = client.get(category, "alert", id.toString)
    val json = Await.result(response.map(_.getResponseBody), config.msTimeout milli)

    serializer.fromJson[Alert](json, Alert.getClass)
  }
}


object ElasticsAlertsRepository {
  // serializer
  private val serializer: Gson = new GsonBuilder().create()

  // elastic search config
  case class ElasticSearchConfig(url: String, msTimeout: Int, ioThreads: Int)

  // specific serializer
  class Serializer extends JsonDeserializer[Alert] {
    override def deserialize(json: JsonElement, typeOfT: Type,
                             context: JsonDeserializationContext): Alert = {
      // deserialize data only
      serializer.fromJson(json.getAsJsonObject.get("_source"), classOf[Alert])
    }
  }

  def createElasticSearchConfig(config: Config): ElasticSearchConfig = {
    ElasticSearchConfig(
      config.getString("conf.elasticsearch.url"),
      config.getInt("conf.elasticsearch.timeout"),
      config.getInt("conf.ioThreadPoolSize")
    )
  }

  def create(config: Config): ElasticsAlertsRepository = {
    new ElasticsAlertsRepository(createElasticSearchConfig(config))
  }
}