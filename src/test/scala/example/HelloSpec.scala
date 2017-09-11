package example

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
}
