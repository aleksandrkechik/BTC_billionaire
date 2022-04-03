package org.btc.frontend.graphql

import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import org.btc.DTOs.{AccountHistory, AccountHistoryHourlyInfo, TransactionReceiveStatus}
import org.btc.frontend.{BtcTransactionJson, EndpointActorResponse}
import org.btc.frontend.graphql.Data.SecureContext
import sangria.macros.derive.deriveObjectType
import sangria.schema._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt



object Schema {

  implicit val accountHistoryHourlyInfo = deriveObjectType[Unit, AccountHistoryHourlyInfo]()
  implicit val accountHistoryType = deriveObjectType[Unit, AccountHistory]()
  implicit val transactionReceiveStatusType = deriveObjectType[Unit, TransactionReceiveStatus]()


  implicit val historyQueryBorders = Argument("historyQueryBorders", StringType)
  implicit val transactionDetailArg = Argument("transactionDetail", StringType)
  //============================================
  val QueryType = ObjectType("Query", fields[SecureContext, Unit](
    Field("getAccountTransferHistory", accountHistoryType,
      arguments = historyQueryBorders :: Nil,
      resolve = ctx => AccountHistory(Seq(AccountHistoryHourlyInfo(1.111, "someDt"), AccountHistoryHourlyInfo(2.222, "anotherDt")))
  )
  ))

  val MutationType = ObjectType("Mutation", fields[SecureContext, Unit](
    Field("sendBitCoins", transactionReceiveStatusType,
      arguments = transactionDetailArg :: Nil,
      resolve = ctx ⇒ {
        val transactionJSON = ctx.arg(transactionDetailArg)
        val response: Future[TransactionReceiveStatus] = (ctx.ctx.endpointActor ? BtcTransactionJson(transactionJSON)).mapTo[TransactionReceiveStatus]
        response
      }
    )
  ))

  def schema = sangria.schema.Schema(QueryType, Some(MutationType))
}
