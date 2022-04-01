package org.btc

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
//import akka.management.cluster.bootstrap.ClusterBootstrap
//import akka.management.scaladsl.AkkaManagement
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object BtcAccountMain extends App {
    val log = LoggerFactory.getLogger(this.getClass)

    val system =
        ActorSystem[Nothing](Behaviors.empty, "BtcAccountsService")
    private val sharding = ClusterSharding(system)

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
    BtcAccount.init(system)
//    try {
//        val orderService = orderServiceClient(system)
//        init(system, orderService)
//    } catch {
//        case NonFatal(e) =>
//            log.error("Terminating due to initialization failure.", e)
//            system.terminate()
//    }
}
