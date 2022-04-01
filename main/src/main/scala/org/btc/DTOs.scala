package org.btc
import java.time.{LocalDateTime, ZonedDateTime}

object DTOs {
  case class BtcTransaction(amount: Double, dt: ZonedDateTime)
  case class BtcStored(amount: Double, dt: LocalDateTime, zoneOffset: Int)
  case class TransactionReceiveStatus(status: String)
  case class AccountStatusQuery(startDt: ZonedDateTime, endDt: ZonedDateTime)


  case class AccountHistoryHourlyInfo(amount: Double, dt: String)
  case class AccountHistory(history: Seq[AccountHistoryHourlyInfo])
}
