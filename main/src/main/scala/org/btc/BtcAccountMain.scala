package org.btc

import akka.actor.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.persistence.typed.PersistenceId
import com.typesafe.config.ConfigFactory
import org.btc.endpoint.{EndpointActor, GraphqlService}
import org.btc.repository.{BtcTransactionsRepositoryImpl, ScalikeJdbcSetup}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

object BtcAccountMain extends App {
  val log = LoggerFactory.getLogger(this.getClass)

  implicit val system = akka.actor.ActorSystem("BtcAccountsService")
  val typedSystem = system.toTyped
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val sharding: ClusterSharding = ClusterSharding(typedSystem)
  ScalikeJdbcSetup.init(typedSystem)
  AkkaManagement(typedSystem).start()
  ClusterBootstrap(typedSystem).start()
  BtcAccount.init(typedSystem)
  val btcTransactionsRepository = new BtcTransactionsRepositoryImpl()
  BtcTransactionsProjection.init(typedSystem, btcTransactionsRepository)
  val accountId = ConfigFactory.load("application.conf").
    getConfig("btc-account").getString("id")
  val accountPersistentId = PersistenceId(BtcAccount.EntityKey.name, accountId)
  val service: BtcAccountService = new BtcAccountService(typedSystem, btcTransactionsRepository)

  initEndpoint()

  def initEndpoint() = {
    val serverAddress = ConfigFactory.load("application.conf").
      getConfig("endpointInfo").getString("serverAddress")
    val port = ConfigFactory.load("application.conf").
      getConfig("endpointInfo").getString("port")

    val userRoutes = initRoutes(service)

    Http().newServerAt(serverAddress, port.toInt).bindFlow(userRoutes)
    log.info("========= Http-server initialized ==============")
  }

  def initRoutes(btcAccountService: BtcAccountService) = {
    val btcAccountActor: ActorRef = system.actorOf(BtcAccountActor.props(btcAccountService))
    val endpointActor: ActorRef = system.actorOf(EndpointActor.props(btcAccountActor), "endpoint-actor")
    new GraphqlService(endpointActor).route
  }
}
