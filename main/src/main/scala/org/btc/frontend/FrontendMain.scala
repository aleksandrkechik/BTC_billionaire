package org.btc.frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import org.slf4j
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

object FrontendMain extends App {
  val log: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getName)
  private val serverAddress = ConfigFactory.load("application.conf").
    getConfig("frontPageInfo").getString("serverAddress")
  private val port = ConfigFactory.load("application.conf").
    getConfig("frontPageInfo").getString("port")

  implicit val system: ActorSystem = ActorSystem("btc-akka")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val userRoutes = initRoutes()

  Http().newServerAt(serverAddress, port.toInt).bindFlow(userRoutes)
  log.info("========= Http-server initialized ==============")

  def initRoutes() =
    new GraphqlService().route
}
