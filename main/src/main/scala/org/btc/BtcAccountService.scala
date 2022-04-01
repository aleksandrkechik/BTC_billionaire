package org.btc

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.util.Timeout
import org.btc.DTOs.BtcTransaction
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeoutException
import scala.concurrent.Future

class BtcAccountService(system: ActorSystem[_]) {

  val log = LoggerFactory.getLogger(this.getClass)
  private val sharding = ClusterSharding(system)
  implicit private val timeout: Timeout =
    Timeout.create(
      system.settings.config.getDuration("btc-account-service.ask-timeout"))
  import system.executionContext

  def transferBTC(accountID: String, transaction: BtcTransaction): Future[String] = {
    log.info(s"Trying to transfer ${transaction.amount} bitcoins. DT: ${transaction.dt}")
    val entityRef = sharding.entityRefFor(BtcAccount.EntityKey, accountID)
    val reply: Future[BtcAccount.AccountStatus] =
      entityRef.askWithStatus(BtcAccount.TransferBtc(transaction, _))
    val response = reply.map(accountStatus => accountStatus.toString)
    convertError(response)
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
