package org.btc

import org.btc.DTOs.BtcTransaction
import scalikejdbc._

import java.sql.Timestamp


trait BtcTransactionsRepository {
  def update(session: ScalikeJdbcSession, datetime: Timestamp, amount: Double): Unit
  def getEventsByTimePeriod(session: ScalikeJdbcSession, start: Timestamp, end: Timestamp): List[BtcTransaction]
}

class BtcTransactionsRepositoryImpl() extends BtcTransactionsRepository {

  override def update(session: ScalikeJdbcSession, datetime: String, amount: Double): Unit = {
    session.db.withinTx { implicit dbSession =>
      // This uses the PostgreSQL `ON CONFLICT` feature
      // Alternatively, this can be implemented by first issuing the `UPDATE`
      // and checking for the updated rows count. If no rows got updated issue
      // the `INSERT` instead.
      sql"""
           INSERT INTO btc_transactions (datetime, amount) VALUES ($datetime, $amount)
           ON CONFLICT (datetime) DO UPDATE SET count = item_popularity.amount + $amount
         """.executeUpdate().apply()
    }
  }

  override def getEventsByTimePeriod(
      session: ScalikeJdbcSession,
      start: Timestamp, end: Timestamp): List[BtcTransaction] = {
    if (session.db.isTxAlreadyStarted) {
      session.db.withinTx { implicit dbSession =>
        select(start, end)
      }
    } else {
      session.db.readOnly { implicit dbSession =>
        select(start, end)
      }
    }
  }

//  case class TransactionInfo(id: Long, datetime: OffsetDateTime, amount: Double)
  object BtcTransaction extends SQLSyntaxSupport[BtcTransaction] {
    override val tableName = "members"
    def apply(rs: WrappedResultSet) = new BtcTransaction(
      rs.offsetDateTime("datetime"), rs.double("amount"))
  }
  private def select(start: Timestamp, end: Timestamp)(implicit dbSession: DBSession) = {
    sql"SELECT * FROM btc_transactions WHERE datetime  >= $start AND datetime < $end"
      .map(rs => BtcTransaction(rs)).toList
      .apply()
  }
}

