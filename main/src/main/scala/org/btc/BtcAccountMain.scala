package org.btc

import akka.actor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.persistence.typed.PersistenceId
import org.btc.DTOs.BtcTransaction
import org.slf4j.LoggerFactory

import java.time.OffsetDateTime
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object BtcAccountMain extends App {
  val log = LoggerFactory.getLogger(this.getClass)

  val system = {
    ActorSystem[Nothing](Behaviors.empty, "BtcAccountsService")
  }
  implicit val materializer: actor.ActorSystem = system.classicSystem
  private val sharding: ClusterSharding = ClusterSharding(system)
  ScalikeJdbcSetup.init(system)
  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  BtcAccount.init(system)
  val accountId = "OOO"
  val accountPersistentId = PersistenceId(BtcAccount.EntityKey.name, accountId)
  val service = new BtcAccountService(system)

  val response1: Future[String] = service.transferBTC("OOO", BtcTransaction(OffsetDateTime.now(), 1.11))
  val accountAfterLastTransaction = Await.result(response1, 10.seconds)
  val historyReader = new BtcAccountHistoryReader(system)
  historyReader.printEventJournal(accountPersistentId.id)
  val eventsByHour = historyReader.getHourTotal(accountPersistentId.id)
  println(eventsByHour)
  println(eventsByHour.values.sum)
  println(accountAfterLastTransaction)
  val hourlyInfo = historyReader.hourlyEventsMapToHistory(eventsByHour)
  println(hourlyInfo)
}
