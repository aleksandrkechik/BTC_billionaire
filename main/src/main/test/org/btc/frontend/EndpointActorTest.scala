package org.btc.frontend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.gson.{Gson, GsonBuilder}
import org.btc.DTOs.{BtcTransaction, StringBtcTransaction}
import org.scalatest.FunSuite

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.{OffsetDateTime, ZonedDateTime}

class EndpointActorTest extends FunSuite {


  case class BtcTransactionWithOffset(dateTime: OffsetDateTime, amount: Double)
  test("ZonedDateTime parsing") {
    val jsonString = "{ \"datetime\": \"2019-10-05T14:45:05+07:00\", \"amount\": 10 }"
//    val mapper: ObjectMapper = (new ObjectMapper).registerModule(new JavaTimeModule).setDateFormat(new SimpleDateFormat("YYYY-MM-DDThh:mm:ssTZD"))
//    val transaction = mapper.readValue(jsonString, classOf[BtcTransaction])
    val gson = new Gson()
    val stringTransaction = gson.fromJson(jsonString, classOf[StringBtcTransaction])
    val transaction = BtcTransaction(ZonedDateTime.parse(stringTransaction.datetime), stringTransaction.amount)
    println(transaction)
  }
}
