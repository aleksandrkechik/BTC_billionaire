package org.btc

import akka.actor.typed.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.scaladsl.{Sink, Source}
import akka.{NotUsed, actor}
import org.btc.BtcAccount.BtcTransferred

import java.time.{Instant, LocalDateTime, ZoneOffset}
import scala.collection.immutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class BtcAccountHistoryReader(system: ActorSystem[_]) {
  val readJournal = {
    PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
  }
  implicit val materializer: actor.ActorSystem = system.classicSystem

  def printEventJournal(persistenceId: String) = {
    val source: Source[EventEnvelope, NotUsed] = readJournal.currentEventsByPersistenceId(persistenceId, 1L, Long.MaxValue)
    source.runForeach { event =>
      println(s"Event: ${event.event}, ${event.timestamp}, " +
        s"${LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timestamp), ZoneOffset.UTC)}" +
        s"${event.eventMetadata}")
    }
  }

  def getCurrentPersistentIds() = {
    readJournal.currentPersistenceIds()
  }

  def getHourTotal(persistenceId: String) = {
    val source: Source[EventEnvelope, NotUsed] = readJournal.currentEventsByPersistenceId(persistenceId, 1L, Long.MaxValue)
    val pairSource: Source[(Double, Int), NotUsed] = source.map{ event =>
      event.event match {
        case transaction: BtcTransferred =>
          val hour = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timestamp), ZoneOffset.UTC).getHour
          (transaction.btcTransaction.amount , hour)
      }
    }
    val sink: Sink[(Double, Int), Future[immutable.Seq[(Double, Int)]]] = Sink.seq
    val pairList = Await.result(pairSource.runWith(sink), 10.seconds)
    pairList.groupBy(_._2).map(e => e._1 -> e._2.map(_._1).sum)
  }
}