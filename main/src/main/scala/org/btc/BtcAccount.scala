package org.btc

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import com.typesafe.config.ConfigFactory
import org.btc.DTOs.BtcTransaction

import java.time.OffsetDateTime
import scala.concurrent.duration.DurationInt

object BtcAccount {
  final case class State(btcAmount: Double, latestTransfer: OffsetDateTime) extends CborSerializable {

    def addTransferredMoneyToAccount(btcTransaction: BtcTransaction): State =
      State(btcTransaction.amount, btcTransaction.datetime)

    def getBtcAmount(): Double =
      this.btcAmount
  }

  object State {
    val empty: State =
      State(1000, OffsetDateTime.now())
  }

  final case class AccountStatus(btcAmount: Double) extends CborSerializable

  sealed trait Command extends CborSerializable

  final case class TransferBtc(btcTransaction: BtcTransaction,
                               replyTo: ActorRef[StatusReply[AccountStatus]])
    extends Command

  sealed trait Event extends CborSerializable {
    def accountId: String
  }

  val EntityKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("BtcAccount")

  final case class BtcTransferred(accountId: String, btcTransaction: BtcTransaction)
    extends Event

  def apply(accountId: String, projectionTag: String): Behavior[Command] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId(EntityKey.name, accountId),
        emptyState = State.empty,
        commandHandler =
          (state, command) => handleCommand(accountId, state, command),
        eventHandler = (state, event) => handleEvent(state, event))
            .withTagger(_ => Set(projectionTag))
      .withRetention(RetentionCriteria
        .snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(
        SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

  def handleCommand(accountId: String, state: State, command: Command): ReplyEffect[Event, State] = {
    command match {
      case TransferBtc(transaction, replyTo) =>
        Effect
        .persist(BtcTransferred(accountId, calculateEventImpact(state, transaction)))
        .thenReply(replyTo) { updatedAccount =>
          StatusReply.Success(AccountStatus(updatedAccount.btcAmount))
        }
    }
  }

  val handleEvent: (State, Event) => State = { (state, event) =>
    event match {
      case BtcTransferred(_, btcTransaction) =>
        state.addTransferredMoneyToAccount(btcTransaction)
    }
  }

  private def calculateEventImpact(state: State, transaction: BtcTransaction) =
    transaction.copy(amount = transaction.amount + state.getBtcAmount())

  def init(system: ActorSystem[_]): Unit = {
    ClusterSharding(system).init(Entity(EntityKey) { entityContext =>
      BtcAccount(entityContext.entityId, ConfigFactory.load("application.conf").
    getConfig("btc-account").getString("id"))
    })
  }

}
