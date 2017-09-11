package example

import java.nio.ByteBuffer

import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord
import org.scalatest._

class EventHandlerSpec extends FlatSpec with Matchers {
  "The EventHandler object" should "decode url" in {
    val eh = new EventHandler()
    eh.decodeS3Key("foo") shouldEqual "foo"
    eh.decodeS3Key("foo%20bar") shouldEqual "foo bar"
  }
  "The EventHandler object" should "processMessageData" in {
    val eh = new EventHandler()
    eh.processMessageData("event-1") shouldEqual("event-1_lambda")
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

    val eh = new EventHandler()
    eh.processEvent(event) shouldBe 1
  }
}
