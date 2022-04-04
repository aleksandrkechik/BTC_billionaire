
package org.btc

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.Offset
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.jdbc.scaladsl.JdbcProjection
import akka.projection.scaladsl.{ExactlyOnceProjection, SourceProvider}
import akka.projection.{ProjectionBehavior, ProjectionId}
import com.typesafe.config.ConfigFactory
import org.btc.repository.{BtcTransactionsRepository, ScalikeJdbcSession}

object BtcTransactionsProjection {
  
  def init(
      system: ActorSystem[_],
      repository: BtcTransactionsRepository): Unit = {
    ShardedDaemonProcess(system).init( 
      name = "BtcTransactionsProjection",
      1,
      index =>
        ProjectionBehavior(createProjectionFor(system, repository)),
      ShardedDaemonProcessSettings(system),
      Some(ProjectionBehavior.Stop))
  }
  

  private def createProjectionFor(
                                   system: ActorSystem[_],
                                   repository: BtcTransactionsRepository)
      : ExactlyOnceProjection[Offset, EventEnvelope[BtcAccount.Event]] = {
    val tag = ConfigFactory.load("application.conf").
    getConfig("btc-account").getString("id")

    val sourceProvider
        : SourceProvider[Offset, EventEnvelope[BtcAccount.Event]] =
      EventSourcedProvider.eventsByTag[BtcAccount.Event](
        system = system,
        readJournalPluginId = JdbcReadJournal.Identifier, tag)

    JdbcProjection.exactlyOnce( 
      projectionId = ProjectionId("BtcTransactionsProjection", tag),
      sourceProvider,
      handler = () =>
        new BtcTransactionsProjectionHandler(tag, system, repository),
      sessionFactory = () => new ScalikeJdbcSession())(system)
  }

}

