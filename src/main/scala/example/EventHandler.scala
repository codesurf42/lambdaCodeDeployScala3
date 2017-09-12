package example

import scala.collection.JavaConverters._
import java.net.URLDecoder
import java.nio.ByteBuffer

import com.amazonaws.{ClientConfiguration, ClientConfigurationFactory}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{KinesisEvent, S3Event}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectResult
import example.messages.{EventsNumber, MessageContent, MessageId}

import scala.collection.mutable.ArrayBuffer

object messages {
  type MessageContent = String
  type MessageId = String
  type EventsNumber = Int

}

object LambdaHandler {
  val s3 = new S3Client
  val eh = new EventHandler(s3.store)
  def processEvent(event: KinesisEvent) = {
    eh.processEvent(event)
  }
}

class S3Client {

  val timeout = 3 * 1000
  val awsConfig = (new ClientConfiguration).withConnectionTimeout(timeout).withRequestTimeout(timeout)
  val s3Client = AmazonS3ClientBuilder.standard().withClientConfiguration(awsConfig).build()
  def store(message: MessageContent): MessageId = {
    println(s"Storing in S3: $message")
    val res = s3Client.putObject("lambda3-storage1", "key1", message)
    println(s"Storing in S3-after: $message")
    s"${res.getETag}--${res.getVersionId}"
    // "ETag+versionId"
  }
}

class EventHandler(storeInS3: MessageContent => MessageId) {

  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  def processEvent(event: KinesisEvent): EventsNumber = {
    println(s"event: $event")
    val res1 = Option(event.getRecords).map(_.asScala).getOrElse(ArrayBuffer.empty)
    println(s"event r1: $res1")
    println(s"event r1: ${res1.size}")

    val events = res1.map { record =>
      println(s"record: $record")
      // log.debug("processing... ")
      val data = record.getKinesis.getData
      
      println(s"processing ${record.getEventID} => $data")

      processMessageData(bytes2String(data))
    }

    events.map { event =>
      storeInS3(event)
    }.size
    
  }

  def processMessageData(in: String): String = {
    println(s"processing data=$in") // TODO logs
    in + "_lambda"
  }

  def bytes2String(buffer: ByteBuffer): String = {
    new String(buffer.array())
  }
}
