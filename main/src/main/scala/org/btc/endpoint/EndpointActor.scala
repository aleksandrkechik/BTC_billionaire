package org.btc.endpoint

import akka.actor
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.google.gson.Gson
import org.btc.BtcAccountMain.system.dispatcher
import org.btc.DTOs.{AccountStatusQuery, BtcTransaction, StringAccountQuery, StringBtcTransaction}
import org.slf4j.LoggerFactory

import java.time.OffsetDateTime

case class BtcTransactionJson(json: String)
case class AccountStatusRequestJson(json: String)
case class EndpointActorResponse(msg: String)


object EndpointActor {
  def props(btcAccountActor: ActorRef) =
    Props(new EndpointActor(btcAccountActor: ActorRef))
}

class EndpointActor(btcAccountActor: ActorRef) extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val gson = new Gson()
  val system = context.system
  implicit val materializer: actor.ActorSystem = context.system.classicSystem
  implicit private val timeout: Timeout =
    Timeout.create(
      system.settings.config.getDuration("btc-account-service.ask-timeout"))


  override def receive: Receive = {

    case BtcTransactionJson(json) =>
      val stringTransaction = gson.fromJson(json, classOf[StringBtcTransaction])
      val transaction: BtcTransaction = BtcTransaction(OffsetDateTime.parse(stringTransaction.datetime), stringTransaction.amount)
      (btcAccountActor ? transaction).pipeTo(sender())

    case AccountStatusRequestJson(json) =>
      val stringQuery = gson.fromJson(json, classOf[StringAccountQuery])
      val query: AccountStatusQuery = AccountStatusQuery(OffsetDateTime.parse(stringQuery.startDatetime), OffsetDateTime.parse(stringQuery.endDatetime))
      (btcAccountActor ? query).pipeTo(sender())
  }
}
