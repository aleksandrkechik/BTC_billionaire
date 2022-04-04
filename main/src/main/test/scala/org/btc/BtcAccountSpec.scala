package scala.org.btc

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import org.btc.BtcAccount
import org.btc.DTOs.BtcTransaction
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.OffsetDateTime

object BtcAccountSpec {
    val config = ConfigFactory
      .parseString("""
        akka.actor.serialization-bindings {
          "org.btc.CborSerializable" = jackson-cbor
        }
        """)
      .withFallback(EventSourcedBehaviorTestKit.config)
  }

  class BtcAccountSpec
      extends ScalaTestWithActorTestKit(BtcAccountSpec.config)
      with AnyWordSpecLike
      with BeforeAndAfterEach {

    private val testAccount = "testAccount"
    private val eventSourcedTestKit =
      EventSourcedBehaviorTestKit[
        BtcAccount.Command,
        BtcAccount.Event,
        BtcAccount.State](system, BtcAccount(testAccount, "testAccount"))

    override protected def beforeEach(): Unit = {
      super.beforeEach()
      eventSourcedTestKit.clear()
    }

    "The BTC account" should {

      "transferBTC" in {
        val transaction = BtcTransaction(datetime = OffsetDateTime.now(), amount = 10)
        val result1 =
          eventSourcedTestKit.runCommand[StatusReply[BtcAccount.AccountStatus]](
            replyTo => BtcAccount.TransferBtc(transaction, replyTo))
        result1.reply should ===(
          StatusReply.Success(
            BtcAccount.AccountStatus(1010)))
        result1.event should ===(BtcAccount.BtcTransferred(testAccount,
          BtcTransaction(transaction.datetime, 1010)))
      }
    }
}
