package example

import scala.collection.JavaConverters._
import java.net.URLDecoder
import java.nio.ByteBuffer

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsyncClientBuilder, AmazonDynamoDBClientBuilder}
import com.amazonaws.{ClientConfiguration, ClientConfigurationFactory}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{KinesisEvent, S3Event}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectResult
import example.messages._

import scala.collection.mutable.ArrayBuffer

object messages {
  type MessageContent = String
  type MessageId = String
  type EventsNumber = Int
  type FactId = String
  type Submission = String

}

object LambdaHandler {
  val s3 = new S3Client
  val dyn = new DynamoDbClient
  val eh = new EventHandler(s3.store, dyn.getFactSubmission)
  def processEvent(event: KinesisEvent) = {
    eh.processEvent(event)
  }
}

class S3Client {

  val timeout = 3 * 1000
  val awsConfig = (new ClientConfiguration).withConnectionTimeout(timeout).withRequestTimeout(timeout)
//  val s3Client = AmazonS3ClientBuilder.standard().withClientConfiguration(awsConfig).build()
  val s3Client = AmazonS3ClientBuilder.defaultClient()
  def store(message: MessageContent): MessageId = {
    println(s"Storing in S3: $message")
    val res = s3Client.putObject("lambda3-storage1", "key1", message)
    println(s"Storing in S3-after: $message")
    s"${res.getETag}--${res.getVersionId}"
    // "ETag+versionId"
  }
}

class DynamoDbClient {
  val timeout = 3 * 1000
  val awsConfig = (new ClientConfiguration()).withConnectionTimeout(timeout).withRequestTimeout(timeout)
  val client = AmazonDynamoDBClientBuilder.defaultClient()
  val docClient = new DynamoDB(client)
  val tableFact = "nr_facts"
  val table = docClient.getTable(tableFact)

  def getFactSubmission(factId: String): Submission = {
    val res = table.getItemOutcome("factId", factId)
    res.getItem.getNumber("submission").toPlainString
  }

}

class EventHandler(storeInS3: MessageContent => MessageId, factRetrieve: FactId => Submission) {

  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  def processEvent(event: KinesisEvent): EventsNumber = {
    println(s"event: $event")
    val res1 = Option(event.getRecords).map(_.asScala).getOrElse(ArrayBuffer.empty)
    println(s"event r1: $res1")
    println(s"event r1: ${res1.size}")

    val events = res1.map { record =>
      println(s"record: $record")
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

    val factId = "GkInqwe897"
    val submission = factRetrieve(factId)
    s"${in}_lambda_dyn:$submission"
  }

  def bytes2String(buffer: ByteBuffer): String = {
    new String(buffer.array())
  }
}
