package example

import scala.collection.JavaConverters._
import java.net.URLDecoder

import com.amazonaws.services.lambda.runtime.events.{KinesisEvent, S3Event}
import com.amazonaws.services.s3.AmazonS3ClientBuilder

class EventHandler {

  val s3Client = AmazonS3ClientBuilder.defaultClient()
  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  def processEvent(event: KinesisEvent) = {
    val events = event.getRecords.asScala.map { record =>
      // log.debug("processing... ")
      val data = record.getKinesis.getData
      println(s"processing ${record.getEventID} => $data")

      processMessageData(data.toString)
    }

    events.foreach { event =>
      storeInS3(event)
    }
    
  }

  def processMessageData(in: String): String = {
    println(s"processing data=$in") // TODO logs
    in + "_lambda"
  }

  def storeInS3(messsage: String) = {
    s3Client.putObject("bucket1", "key1", messsage)
  }

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
