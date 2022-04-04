package org.btc

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.typesafe.config.ConfigFactory
import org.btc.DTOs.{AccountHistory, AccountStatusQuery, BtcTransaction}

object BtcAccountActor {
  def props(service: BtcAccountService) = Props(new BtcAccountActor(service: BtcAccountService))
}

class BtcAccountActor(service: BtcAccountService) extends Actor{
  import org.btc.BtcAccountMain.executionContext
  override def receive: Receive = {
    case transaction: BtcTransaction =>
      val transactionResult = service.transferBTC(ConfigFactory.load("application.conf").
    getConfig("btc-account").getString("id"), transaction)
      transactionResult.pipeTo(sender())
    case accountStatusQuery: AccountStatusQuery =>
      val hourlyInfo = service.getHourlyInfo(accountStatusQuery.startDt, accountStatusQuery.endDt)
      val historyJson = service.hourlyInfoToHistoryJson(hourlyInfo)
      val accountHistory = AccountHistory(historyJson)
      sender() ! accountHistory
  }
}
