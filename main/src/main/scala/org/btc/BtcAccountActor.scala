package org.btc

import akka.actor.Actor
import org.btc.DTOs.{AccountStatusQuery, BtcTransaction}
import org.slf4j
import org.slf4j.LoggerFactory

class BtcAccountActor  extends Actor{
  val log: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getName)

  override def receive: Receive = {
    case transaction: BtcTransaction =>
      log.info(s"Got transaction! Amount: ${transaction.amount}. Transaction time: ${transaction.dt}")
      sender() ! "OK"
  }
}
