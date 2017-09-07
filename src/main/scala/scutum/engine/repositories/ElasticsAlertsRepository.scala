package scutum.engine.repositories

import java.time._
import wabisabi.Client
import com.google.gson._
import scala.concurrent._
import java.lang.reflect._
import com.typesafe.config._
import java.time.temporal._
import scala.concurrent.duration._
import java.util.concurrent.Executors
import scutum.core.contracts.Alert
import scutum.engine.repositories.ElasticsAlertsRepository._

class ElasticsAlertsRepository(config: ElasticSearchConfig) {
  private val client = new Client(config.url)
  private val serializer: Gson = new GsonBuilder()
    .registerTypeAdapter(classOf[Entry], new Serializer).create()
  private val threadsPool = Executors.newFixedThreadPool(config.ioThreads)
  private implicit val context = ExecutionContext.fromExecutor(threadsPool)

  def create(category: String, alert: Alert): Long = {
    val f = client.verifyIndex(category)
    val result = Await.result(f.map(_.getStatusCode), config.msTimeout milli)
    if(result != 200) client.createIndex(category)

    val id = ElasticsAlertsRepository.createId(alert.getDetails)
    client.index(category, "alert", Some(id.toString), serializer.toJson(Entry(alert)), true)
    id
  }

  def read(category: String, id: Long): Alert = {
    val response = client.get(category, "alert", id.toString)
    val json = Await.result(response.map(_.getResponseBody), config.msTimeout milli)
    serializer.fromJson[Entry](json, classOf[Entry]).alert
  }
}


object ElasticsAlertsRepository {
  private val epoch = LocalDateTime.of(2017, 1, 1, 0, 0, 0)
  private val serializer: Gson = new GsonBuilder().create()

  case class ElasticSearchConfig(url: String, msTimeout: Int, ioThreads: Int)
  case class Entry(alert: Alert, timestamp: String = LocalDateTime.now(ZoneId.of("UTC")).toString)

  // specific serializer
  class Serializer extends JsonDeserializer[Entry] {
    override def deserialize(json: JsonElement, typeOfT: Type,
                             context: JsonDeserializationContext): Entry = {
      // deserialize data only
      serializer.fromJson(json.getAsJsonObject.get("_source"), classOf[Entry])
    }
  }

  def createElasticSearchConfig(config: Config): ElasticSearchConfig = {
    ElasticSearchConfig(
      config.getString("conf.elasticsearch.url"),
      config.getInt("conf.elasticsearch.timeoutMs"),
      config.getInt("conf.ioThreadPoolSize")
    )
  }

  def create(config: Config): ElasticsAlertsRepository = {
    new ElasticsAlertsRepository(createElasticSearchConfig(config))
  }

  def createId(text: String): Long = {
    val now = LocalDateTime.now(ZoneId.of("UTC"))
    epoch.until(now, ChronoUnit.MILLIS) << 20
  }
}