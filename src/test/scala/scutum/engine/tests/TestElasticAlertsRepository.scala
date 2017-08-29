package scutum.engine.tests

import org.scalatest.WordSpecLike
import scutum.engine.contracts.Alert
import scutum.engine.repositories.ElasticsAlertsRepository

class TestElasticAlertsRepository extends WordSpecLike{
  "Kafka repository" must {
    "Common kafka client tests" in {
      val config = ElasticsAlertsRepository.Config("http://localhost:9200", 100, 8)
      val repo = new ElasticsAlertsRepository(config)

      val in = Alert(101, "Some alert")
      repo.create("test", in)

      val out = repo.read("test", 101)
      assert(in == out)
    }
  }
}
