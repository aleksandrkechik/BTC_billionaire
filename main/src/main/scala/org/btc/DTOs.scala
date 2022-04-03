package org.btc
import com.fasterxml.jackson.annotation.{JsonCreator, JsonFormat}

import java.time.{LocalDateTime, ZonedDateTime}

object DTOs {
  case class StringBtcTransaction(datetime: String, amount: Double)
  case class BtcTransaction
//  @JsonCreator() (@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-DDThh:mm:ssTZD")
  (datetime: ZonedDateTime, amount: Double)
//  {
//    def this(rawTransaction: StringBtcTransaction) =
//      this(ZonedDateTime.parse(rawTransaction.datetime), rawTransaction.amount)
//  }
  case class TransactionReceiveStatus(status: String)
  case class AccountStatusQuery(startDt: ZonedDateTime, endDt: ZonedDateTime)


  case class AccountHistoryHourlyInfo(amount: Double, dt: String)
  case class AccountHistory(history: Seq[AccountHistoryHourlyInfo])
}
