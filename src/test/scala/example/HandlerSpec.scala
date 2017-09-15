package example

import java.nio.ByteBuffer

import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord
import example.messages._
import org.scalatest._

class EventHandlerSpec extends FlatSpec with Matchers {

  def s3Put(s:MessageContent): MessageId = "id"
  def dynamoDbGetFact(s:FactId): Submission = "dynamoSub"
  def ehBuild = new EventHandler(s3Put, dynamoDbGetFact)

  "The EventHandler object" should "decode url" in {
    val eh = ehBuild
    eh.decodeS3Key("foo") shouldEqual "foo"
    eh.decodeS3Key("foo%20bar") shouldEqual "foo bar"
  }
  "The EventHandler object" should "processMessageData" in {
    val eh = ehBuild
    eh.processMessageData("event-1") should startWith("event-1_lambda")
    eh.processMessageData("event-1") should include("dynamoSub")
  }

  "The EventHandler" should "processEvent" in {

    val kRecord = new KinesisEvent.Record
    kRecord.setData(ByteBuffer.wrap("foo123".getBytes()))
    val record = new KinesisEventRecord()
    record.setKinesis(kRecord)
    record.setEventID("123")
    record.setEventSource("source")
    val event = new KinesisEvent()
    event.setRecords(List(record).asJava)

    val eh = ehBuild
    eh.processEvent(event) shouldBe 1
  }
}
