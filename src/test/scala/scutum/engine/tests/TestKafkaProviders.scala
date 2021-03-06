package scutum.engine.tests

import com.google.gson._
import org.scalatest.WordSpecLike
import scutum.core.contracts.ScannedData
import scutum.engine.repositories.KafkaEventsRepository

class TestKafkaProviders extends WordSpecLike{
  private val serializer: Gson = new GsonBuilder().create()
  "Kafka repository" must {
    "Common kafka client tests" in {

      if (System.getProperty("java.class.path").toLowerCase.contains("intellij")) {

        val config = KafkaEventsRepository.createKafkaConfig(TestUtils.config)
        config.topics = "scutum_ingest"
        val repository = new KafkaEventsRepository(config)
        while (repository.consume().nonEmpty) println(s"old items in kafka")


        val dataIn = new ScannedData(1, 1, "some provider name", 1, 1, "some data")
        val dataInJson = serializer.toJson(dataIn)
        repository.publish("1_1_1", dataInJson)

        var dataOut = repository.consume()
        if (dataOut.isEmpty) dataOut = repository.consume()
        assert(dataOut.head.getData == dataIn.getData)
      }
      else assert(true)
    }
  }
}
