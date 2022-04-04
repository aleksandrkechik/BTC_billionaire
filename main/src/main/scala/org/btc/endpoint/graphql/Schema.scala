package org.btc.endpoint.graphql

import akka.pattern.ask
import akka.util.Timeout
import org.btc.BtcAccountMain.executionContext
import org.btc.DTOs.{AccountHistory, AccountHistoryHourlyInfo, TransactionReceiveStatus}
import org.btc.endpoint.graphql.Data.SecureContext
import org.btc.endpoint.{AccountStatusRequestJson, BtcTransactionJson}
import sangria.macros.derive.deriveObjectType
import sangria.schema.{Argument, Field, ObjectType, StringType, fields}

import java.util.concurrent.TimeUnit

object Schema {

  implicit private val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

  implicit val accountHistoryHourlyInfo = deriveObjectType[Unit, AccountHistoryHourlyInfo]()
  implicit val accountHistoryType = deriveObjectType[Unit, AccountHistory]()
  implicit val transactionReceiveStatusType = deriveObjectType[Unit, TransactionReceiveStatus]()


  implicit val historyQueryBorders = Argument("historyQueryBorders", StringType)
  implicit val transactionDetailArg = Argument("transactionDetail", StringType)
  //============================================
  val QueryType = ObjectType("Query", fields[SecureContext, Unit](
    Field("getAccountTransferHistory", accountHistoryType,
      arguments = historyQueryBorders :: Nil,
      resolve = ctx => {
        val transactionJSON = ctx.arg(historyQueryBorders)
        val response = (ctx.ctx.endPointActor ? AccountStatusRequestJson(transactionJSON)).mapTo[AccountHistory]
        response
      }
    )
  ))

  val MutationType = ObjectType("Mutation", fields[SecureContext, Unit](
    Field("sendBitCoins", transactionReceiveStatusType,
      arguments = transactionDetailArg :: Nil,
      resolve = ctx ⇒ {
        val transactionJSON = ctx.arg(transactionDetailArg)
        val response = (ctx.ctx.endPointActor ? BtcTransactionJson(transactionJSON)).
          mapTo[String].map(e => TransactionReceiveStatus(e))
        response
      }
    )
  ))

  def schema = sangria.schema.Schema(QueryType, Some(MutationType))
}
