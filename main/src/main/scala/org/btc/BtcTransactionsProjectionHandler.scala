
package org.btc

import akka.actor.typed.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import akka.projection.jdbc.scaladsl.JdbcHandler
import org.slf4j.LoggerFactory

import java.sql.Timestamp

class BtcTransactionsProjectionHandler(
    tag: String,
    system: ActorSystem[_],
    repo: BtcTransactionsRepository)
    extends JdbcHandler[
      EventEnvelope[BtcAccount.Event],
      ScalikeJdbcSession]() { 

  private val log = LoggerFactory.getLogger(getClass)

  override def process(
      session: ScalikeJdbcSession,
      envelope: EventEnvelope[BtcAccount.Event]): Unit = {
    envelope.event match { 
      case BtcAccount.BtcTransferred(_, btcTransaction) =>
        repo.update(session, Timestamp.valueOf(btcTransaction.datetime.toLocalDateTime), btcTransaction.amount)
        logTransfer(session, btcTransaction.amount)
    }
  }

  private def logTransfer(
      session: ScalikeJdbcSession,
      amount: Double): Unit = {
    log.info(s"${amount} BTC was transferred")
  }

}

