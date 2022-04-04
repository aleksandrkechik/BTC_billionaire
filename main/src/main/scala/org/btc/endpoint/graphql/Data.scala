package org.btc.endpoint.graphql

import akka.actor.ActorRef

object Data {

  case class SecureContext(endPointActor: ActorRef)
}
