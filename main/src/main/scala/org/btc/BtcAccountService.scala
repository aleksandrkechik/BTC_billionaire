package org.btc

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.btc.DTOs.{AccountHistoryHourlyInfo, BtcTransaction, TransactionReceiveStatus}
import org.btc.repository.{BtcTransactionsRepositoryImpl, ScalikeJdbcSession}
import org.slf4j.LoggerFactory

import java.sql.Timestamp
import java.time.temporal.ChronoUnit
import java.time.{OffsetDateTime, ZoneOffset}
import java.util.concurrent.TimeoutException
import scala.concurrent.Future

class BtcAccountService(system: ActorSystem[_], btcTransactionsRepository: BtcTransactionsRepositoryImpl){

  val log = LoggerFactory.getLogger(this.getClass)
  private val sharding = ClusterSharding(system)
  private val gson = new Gson()
  implicit private val timeout: Timeout =
    Timeout.create(
      system.settings.config.getDuration("btc-account-service.ask-timeout"))
  import system.executionContext

  def transferBTC(accountID: String, transaction: BtcTransaction) = {
    log.info(s"Trying to transfer ${transaction.amount} bitcoins. DT: ${transaction.datetime}")
    val entityRef: EntityRef[BtcAccount.Command] = sharding.entityRefFor(BtcAccount.EntityKey, accountID)
    val reply: Future[BtcAccount.AccountStatus] = {
      entityRef.askWithStatus(BtcAccount.TransferBtc(transaction, _))
    }
    val response: Future[String] = reply.map(accountStatus => accountStatus.toString)
    convertError(response)
  }

  def getHourlyInfo(start: OffsetDateTime, end: OffsetDateTime): Seq[AccountHistoryHourlyInfo] = {
    val unitizedStart = start.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime
    val unitizedEnd = end.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime.truncatedTo(ChronoUnit.HOURS)
    val session = new ScalikeJdbcSession()
    val eventsByHour = btcTransactionsRepository.getEventsByTimePeriod(session,
      Timestamp.valueOf(unitizedStart), Timestamp.valueOf(unitizedEnd)).map(transaction =>
      (transaction.amount, transaction.datetime.toLocalDateTime.plusHours(1))).map { event =>
      AccountHistoryHourlyInfo(event._2.toString + ":00+00:00", event._1)
    }
    eventsByHour
  }

  val mapper = new ObjectMapper()
  def hourlyInfoToHistoryJson(hourlyInfo: Seq[AccountHistoryHourlyInfo]) = {
    val javaStyleHourlyEvents = scala.collection.JavaConverters.seqAsJavaList(hourlyInfo)
    gson.toJson(javaStyleHourlyEvents)
  }

  private def convertError[T](response: Future[T]): Future[T] = {
    response.recoverWith {
      case _: TimeoutException =>
        Future.failed(
          new Exception("Operation timed out"))
      case exc =>
        Future.failed(
          new Exception(exc.getMessage))
    }
  }
}
