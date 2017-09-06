package scutum.engine.tests

import org.scalatest.WordSpecLike
import scutum.core.contracts.Alert
import scutum.engine.repositories.ElasticsAlertsRepository

class TestElasticAlertsRepository extends WordSpecLike{
  "Kafka repository" must {
    "Common kafka client tests" in {
      val config = ElasticsAlertsRepository
        .ElasticSearchConfig("http://localhost:9200", 100, 8)
      val repo = new ElasticsAlertsRepository(config)

      val in = new Alert("Some alert")
      val id = repo.create("test", in)

      Thread.sleep(200)
      val out = repo.read("test", id)
      assert(in.getDetails == out.getDetails)
    }
  }
}
