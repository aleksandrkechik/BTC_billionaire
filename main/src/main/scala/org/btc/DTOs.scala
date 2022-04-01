package org.btc
import java.time.ZonedDateTime

object DTOs {
  case class BtcTransaction(amount: Double, dt: ZonedDateTime)
  case class TransactionReceiveStatus(status: String)
  case class AccountStatusQuery(startDt: ZonedDateTime, endDt: ZonedDateTime)


  case class AccountHistoryHourlyInfo(amount: Double, dt: String)
  case class AccountHistory(history: Seq[AccountHistoryHourlyInfo])
}
