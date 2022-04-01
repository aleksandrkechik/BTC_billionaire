package org.btc

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityContext, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import org.btc.DTOs.BtcTransaction

import java.time.ZonedDateTime
import scala.concurrent.duration.DurationInt

object BtcAccount {
  final case class State(btcAmount: Double, latestTransfer: ZonedDateTime) {

    def addTransferredMoneyToAccount(btcTransaction: BtcTransaction): State =
      State(this.btcAmount + btcTransaction.amount, btcTransaction.dt)
  }
  //TODO - think of it!!!!
  object State {
    val empty: State =
      State(1000, ZonedDateTime.now())
  }
  //TODO - and think of it
  final case class AccountStatus(btcAmount: Double)

  sealed trait Command
  final case class TransferBtc(btcTransaction: BtcTransaction,
                               replyTo: ActorRef[StatusReply[AccountStatus]])
    extends Command

  //TODO - and of this.
  sealed trait Event {
    def accountId: String
  }
//  //TODO - and of this
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
//      .withTagger(_ => Set(projectionTag))
      .withRetention(RetentionCriteria
        .snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(
        SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

  def handleCommand(accountId: String, state: State, command: Command): ReplyEffect[Event, State] =
    command match {
      case TransferBtc(transaction, replyTo) => Effect
        .persist(BtcTransferred(accountId, transaction))
        .thenReply(replyTo) { updatedAccount =>
          StatusReply.Success(AccountStatus(updatedAccount.btcAmount))
        }
    }

  val handleEvent: (State, Event) => State = { (state, event) =>
    event match {
      case BtcTransferred(_, btcTransaction) => state.addTransferredMoneyToAccount(btcTransaction)
    }
  }

//  val tags = Vector.tabulate(5)(i => s"accounts-$i")
//  def init(system: ActorSystem[_]): Unit = {
//    val behaviorFactory: EntityContext[Command] => Behavior[Command] = {
//      entityContext =>
//        val i = math.abs(entityContext.entityId.hashCode % tags.size)
//        val selectedTag = tags(i)
//        println(entityContext.entityId)
//        BtcAccount(entityContext.entityId, selectedTag)
//    }
//    ClusterSharding(system).init(Entity(EntityKey)(behaviorFactory))
//  }

  def init(system: ActorSystem[_]): Unit = {
    ClusterSharding(system).init(Entity(EntityKey) { entityContext =>
      println("aa")
      println(entityContext.entityId)
      BtcAccount(entityContext.entityId, "a")
    })
  }
}
