package org.btc.repository

import org.btc.DTOs.BtcTransaction
import scalikejdbc._

import java.sql.Timestamp


trait BtcTransactionsRepository {
  def update(session: ScalikeJdbcSession, datetime: Timestamp, amount: Double): Unit
  def getEventsByTimePeriod(session: ScalikeJdbcSession, start: Timestamp, end: Timestamp): List[BtcTransaction]
}

class BtcTransactionsRepositoryImpl() extends BtcTransactionsRepository {

  override def update(session: ScalikeJdbcSession, datetime: Timestamp, amount: Double): Unit = {
    session.db.withinTx { implicit dbSession =>
      sql"""
           INSERT INTO btc_transactions (datetime, amount) VALUES ($datetime, $amount)
           ON CONFLICT (datetime) DO UPDATE SET amount = $amount
         """.executeUpdate.apply()
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

  object BtcTransaction extends SQLSyntaxSupport[BtcTransaction] {
    override val tableName = "members"
    def apply(rs: WrappedResultSet) = new BtcTransaction(
      rs.offsetDateTime("datetime"), rs.double("amount"))
  }
  private def select(start: Timestamp, end: Timestamp)(implicit dbSession: DBSession) = {
    sql"""SELECT date_trunc('hour', datetime) as datetime, max(amount) as amount FROM btc_transactions
         WHERE datetime  >= $start AND datetime < $end
         GROUP BY date_trunc('hour', datetime)"""
      .map(rs => BtcTransaction(rs)).toList
      .apply()
  }
}

