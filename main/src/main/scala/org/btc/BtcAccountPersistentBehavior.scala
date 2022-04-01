package org.btc

//import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import org.btc.DTOs.BtcTransaction


class BtcAccountPersistentBehavior {

  sealed trait Command
  final case class AddMoneyToAccount(transferInfo: BtcTransaction) extends Command

  sealed trait Event
  final case class MoneyAdded(transferInfo: BtcTransaction) extends Event

  final case class State(history: List[BtcTransaction] = Nil)

  def onCommand(subscriber: ActorRef[State], state: State, command: Command): Effect[Event, State] = {
    command match {
      case AddMoneyToAccount(transferInfo) =>
        Effect.persist(MoneyAdded(transferInfo)).thenRun(newState => subscriber ! newState)
    }
  }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = State(Nil),
      commandHandler = commandHandler,
      eventHandler = eventHandler)

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case AddMoneyToAccount(transaction) => Effect.persist(MoneyAdded(transaction))
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case MoneyAdded(data) => state.copy((data :: state.history).take(100))
    }
  }
}
