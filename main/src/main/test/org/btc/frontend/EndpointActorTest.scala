package org.btc.frontend

import com.google.gson.Gson
import org.btc.DTOs.{BtcTransaction, StringBtcTransaction}
import org.scalatest.FunSuite

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class EndpointActorTest extends FunSuite {


  case class BtcTransactionWithOffset(dateTime: OffsetDateTime, amount: Double)
  test("OffsetDateTime parsing") {
    val jsonString = "{ \"datetime\": \"2019-10-05T14:45:05+07:00\", \"amount\": 10 }"
//    val mapper: ObjectMapper = (new ObjectMapper).registerModule(new JavaTimeModule).setDateFormat(new SimpleDateFormat("YYYY-MM-DDThh:mm:ssTZD"))
//    val transaction = mapper.readValue(jsonString, classOf[BtcTransaction])
    val gson = new Gson()
    val stringTransaction = gson.fromJson(jsonString, classOf[StringBtcTransaction])
    val transaction = BtcTransaction(OffsetDateTime.parse(stringTransaction.datetime), stringTransaction.amount)
    println(transaction)
  }

  test("Zoned DateTimeRounding") {
    val zdt: OffsetDateTime = OffsetDateTime.now()
    val zdt2: OffsetDateTime = zdt.truncatedTo(ChronoUnit.HOURS)
    println(zdt)
    println(zdt2)
  }
}
