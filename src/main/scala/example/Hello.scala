package example

import scala.collection.JavaConverters._
import java.net.URLDecoder
import com.amazonaws.services.lambda.runtime.events.S3Event

class Main {
  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  def getSourceBuckets(event: S3Event): java.util.List[String] = {
    val res1 = event.getRecords
    val res2 = res1.asScala
    val res3 = res2.map { record =>
      decodeS3Key(record.getS3.getObject.getKey)
    }
    val res4 = res3
    println(s"res: $res4")
    res4.asJava
  }
}
