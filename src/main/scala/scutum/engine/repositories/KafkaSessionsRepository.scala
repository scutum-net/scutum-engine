package scutum.engine.repositories

import java.util.UUID
import com.google.gson._
import com.typesafe.config._
import org.apache.kafka.clients._
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.config.SslConfigs
import scutum.engine.repositories.KafkaSessionsRepository._

class KafkaSessionsRepository(config: KafkaConfig) {
  private val topic = config.topics
  private val consumer = createConsumer()
  private val producer = createProducer()


  def publish(data: Seq[(String, String)]): Unit = {
    data.take(data.length - 1).foreach(i => publish(i._1, i._2, flush = false))
    publish(data.last._1, data.last._2)
  }

  def publish(key: String, data: String, flush: Boolean = true): Unit = {
    val record = new ProducerRecord[String, String](topic, key, data)
    producer.send(record)
    if(flush) producer.flush()
  }

  def consume(): Seq[(String, String)] = {
    val data = consumer.poll(config.pollTimeout)
    data.asScala.map(i => (i.key(), i.value())).toSeq
  }

  private def createProducer(): KafkaProducer[String, String] = {
    val props = new java.util.Properties()
    props.put("bootstrap.servers", config.brokers)
    props.put("client.id", config.clientId)
    props.put("enable.auto.commit", config.autoCommit.toString)
    props.put("auto.offset.reset", config.offsetReset)
    props.put("compression.type", config.compressionType)
    props.put("key.serializer", config.keySerializer)
    props.put("value.serializer", config.valueSerializer)

    if(config.securityEnabled) defineSSL(props)
    new KafkaProducer[String, String](props)
  }

  private def createConsumer() = {
    val props = new java.util.Properties()
    props.put("bootstrap.servers", config.brokers)
    props.put("group.id", config.groupId)
    props.put("client.id", config.clientId)
    props.put("enable.auto.commit", config.autoCommit.toString)
    props.put("auto.offset.reset", config.offsetReset)
    props.put("key.deserializer", config.keyDeserializer)
    props.put("value.deserializer", config.valueDeserializer)
    props.put("compression.type", config.compressionType)
    props.put("max.poll.records", config.maxPollSize.toString)

    if(config.securityEnabled) defineSSL(props)
    val consumer = new KafkaConsumer[String, String](props)
    val topicsCollection = config.topics.split(",").toSeq.asJavaCollection
    consumer.subscribe(topicsCollection)
    consumer
  }

  private def defineSSL(props: java.util.Properties) = {
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
    props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  config.sslPassword)
    props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, config.sslEncryptionFile)
    props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, config.sslPassword)
    props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, config.sslPassword)
    props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, config.sslEncryptionFile)
  }
}


object KafkaSessionsRepository {
  val serializer: Gson = new GsonBuilder().create()
  private def getClientId = UUID.randomUUID().toString

  case class KafkaConfig(var topics: String,
                         brokers: String,
                         groupId: String,
                         clientId: String,
                         maxPollSize: Int,
                         pollTimeout: Int,
                         autoCommit: Boolean,
                         offsetReset: String,
                         expirationMins: Int,
                         keySerializer: String,
                         valueSerializer: String,
                         keyDeserializer: String,
                         valueDeserializer: String,
                         compressionType: String,
                         securityEnabled: Boolean,
                         sslPassword: String,
                         sslEncryptionFile: String)



  def createKafkaConfig(config: Config): KafkaConfig = {
    KafkaConfig(
      config.getString("conf.kafka.topics"),
      config.getString("conf.kafka.brokers"),
      config.getString("conf.kafka.groupId"),
      getClientId,
      config.getInt("conf.kafka.maxpollrecords"),
      config.getInt("conf.kafka.maxpolltimeout"),
      config.getBoolean("conf.kafka.autoCommit"),
      config.getString("conf.kafka.offsetReset"),
      config.getInt("conf.kafka.expirationMins"),
      config.getString("conf.kafka.keySerializer"),
      config.getString("conf.kafka.valueSerializer"),
      config.getString("conf.kafka.keyDeserializer"),
      config.getString("conf.kafka.valueDeserializer"),
      config.getString("conf.kafka.compression"),
      config.getBoolean("conf.kafka.useSSL"),
      config.getString("conf.kafka.password"),
      config.getString("conf.kafka.encryptionFile"))
  }

  def create(config: Config): KafkaSessionsRepository = {
    new KafkaSessionsRepository(createKafkaConfig(config))
  }
}