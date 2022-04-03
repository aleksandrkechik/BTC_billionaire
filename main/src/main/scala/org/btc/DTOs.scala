package org.btc
import java.time.OffsetDateTime

object DTOs {
  case class StringBtcTransaction(datetime: String, amount: Double)
  case class BtcTransaction
//  @JsonCreator() (@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-DDThh:mm:ssTZD")
  (datetime: OffsetDateTime, amount: Double)
//  {
//    def this(rawTransaction: StringBtcTransaction) =
//      this(OffsetDateTime.parse(rawTransaction.datetime), rawTransaction.amount)
//  }
  case class TransactionReceiveStatus(status: String)
  case class AccountStatusQuery(startDt: OffsetDateTime, endDt: OffsetDateTime)


  case class AccountHistoryHourlyInfo(datetime: String, amount: Double)
  case class AccountHistory(history: Seq[AccountHistoryHourlyInfo])
}
