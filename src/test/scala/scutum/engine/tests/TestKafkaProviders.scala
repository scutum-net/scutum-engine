package scutum.engine.tests

import com.google.gson._
import org.scalatest.WordSpecLike
import scutum.engine.contracts.external.ScanEvent
import scutum.engine.repositories.KafkaEventsRepository

class TestKafkaProviders extends WordSpecLike{
  private val serializer: Gson = new GsonBuilder().create()
  "Kafka repository" must {
    "Common kafka client tests" in {

      if(System.getProperty("java.class.path").toLowerCase.contains("intellij")) {

        val config = KafkaEventsRepository.createKafkaConfig(TestUtils.config)
        config.topics = "test_topic"
        val repository = new KafkaEventsRepository(config)
        while (repository.consume().nonEmpty) println(s"old items in kafka")


        val dataIn = ScanEvent(1,1,1, "some data")
        repository.publish("1_1_1", serializer.toJson(dataIn))

        var dataOut = repository.consume()
        if (dataOut.isEmpty) dataOut = repository.consume()
        assert(dataOut.head == dataIn)
      }
      else assert(true)
    }
  }
}
