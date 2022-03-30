import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId


class BtcAccountPersistentBehaviour {
  sealed trait Command
  sealed trait Event
  final case class State()

//  def apply(): Behavior[Command] =
//    EventSourcedBehavior[Command, Event, State](
//      persistenceId = PersistenceId.ofUniqueId("abc"),
//      emptyState = State(),
//      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
//      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state"))
}
