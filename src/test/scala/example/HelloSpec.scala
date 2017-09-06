package example

import org.scalatest._

class HelloSpec extends FlatSpec with Matchers {
  "The Hello object" should "decode url" in {
    val m = new Main()
    m.decodeS3Key("foo") shouldEqual "foo"
    m.decodeS3Key("foo%20bar") shouldEqual "foo bar"
  }
}
