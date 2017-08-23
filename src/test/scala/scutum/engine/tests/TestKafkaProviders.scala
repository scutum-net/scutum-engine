package scutum.engine.tests

import java.util.UUID
import org.scalatest.WordSpecLike
import scutum.engine.repositories.KafkaEventsRepository

class TestKafkaProviders extends WordSpecLike{
  "Kafka repository" must {
    "Common kafka client tests" in {
      val config = KafkaEventsRepository.createKafkaConfig(TestUtils.config)
      config.topics = "test_topic"
      val repository =  new KafkaEventsRepository(config)
      while(repository.consume().nonEmpty) println(s"old items in kafka")

      val dataIn = ("some key", UUID.randomUUID().toString)
      repository.publish(dataIn._1, dataIn._2)

      var dataOut = repository.consume()
      if(dataOut.isEmpty) dataOut = repository.consume()
      assert(dataOut.head == dataIn)
    }
  }
}
