package org.btc

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory
import org.btc.DTOs.BtcTransaction
import org.slf4j.LoggerFactory

import java.time.ZonedDateTime
import scala.util.control.NonFatal

object BtcAccountMain extends App {
    val log = LoggerFactory.getLogger(this.getClass)

    val system =
        ActorSystem[Nothing](Behaviors.empty, "BtcAccountsService")
    private val sharding: ClusterSharding = ClusterSharding(system)
//    ScalikeJdbcSetup.init(system)
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    BtcAccount.init(system)

//    val accountId = BtcAccount
    val service = new BtcAccountService(system)

    service.transferBTC("OOO", BtcTransaction(1.11, ZonedDateTime.now()))

}


