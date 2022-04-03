package org.btc.frontend

import akka.actor
import akka.actor.{Actor, ActorSystem, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.google.gson.Gson
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.btc.DTOs.{BtcTransaction, StringBtcTransaction, TransactionReceiveStatus}
import org.btc.frontend.FrontendMain.system.log
import org.slf4j.LoggerFactory

import java.time.OffsetDateTime

case class BtcTransactionJson(json: String)
case class EndpointActorResponse(msg: String)

object EndpointActor {
  def props(system: ActorSystem) = Props(new EndpointActor(system: ActorSystem))
}

class EndpointActor(system: ActorSystem) extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val gson = new Gson()

  implicit val materializer: actor.ActorSystem = system.classicSystem
  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)

  override def receive: Receive = {

    case BtcTransactionJson(json) =>
      try {
        println(json)
        val stringTransaction = gson.fromJson(json, classOf[StringBtcTransaction])
        val transaction = BtcTransaction(OffsetDateTime.parse(stringTransaction.datetime), stringTransaction.amount)
        sendToKafka("btc-transactions", json)
        sender() ! TransactionReceiveStatus("Transaction success")
      } catch {
        case e =>
          println(e.getMessage)
          sender() ! TransactionReceiveStatus(s"Transaction failure. Please try again. ${e.getMessage}")
      }

  }

  def sendToKafka(topic: String, v: String): Unit = {
    log.info(s"Sending to kafka: $topic")
    Source(List(v)).map(_.toString).map(value => new ProducerRecord[String, String](topic, value))
      .runWith(Producer.plainSink(producerSettings))
  }
}
