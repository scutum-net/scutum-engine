package scutum.engine.contracts.external

trait Processor {
  def getScanType: Int
  def process(event: ScanEvent): Seq[Alert]
}
