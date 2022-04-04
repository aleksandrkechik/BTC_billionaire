package org.btc
import java.time.OffsetDateTime

object DTOs {
  case class StringBtcTransaction(datetime: String, amount: Double)
  case class BtcTransaction(datetime: OffsetDateTime, amount: Double)

  case class TransactionReceiveStatus(status: String)
  case class StringAccountQuery(startDatetime: String, endDatetime: String)
  case class AccountStatusQuery(startDt: OffsetDateTime, endDt: OffsetDateTime)


  case class AccountHistoryHourlyInfo(datetime: String, amount: Double)
  case class AccountHistory(history: String)
}
