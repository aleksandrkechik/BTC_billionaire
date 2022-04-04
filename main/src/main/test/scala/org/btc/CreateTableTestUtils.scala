package scala.org.btc

import akka.Done
import akka.actor.typed.ActorSystem
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import akka.projection.jdbc.scaladsl.JdbcProjection
import org.btc.repository.ScalikeJdbcSession
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

object CreateTableTestUtils {

  private val createBtcTransactionsTableFile =
    Paths.get("ddl-scripts/create_btc_transactions_table.sql").toFile

  def dropAndRecreateTables(system: ActorSystem[_]): Unit = {
    implicit val sys: ActorSystem[_] = system
    implicit val ec: ExecutionContext = system.executionContext

    // ok to block here, main thread
    Await.result(
      for {
        _ <- SchemaUtils.dropIfExists()
        _ <- SchemaUtils.createIfNotExists()
        _ <- JdbcProjection.dropTablesIfExists(() =>
          new ScalikeJdbcSession())
        _ <- JdbcProjection.createTablesIfNotExists(() =>
          new ScalikeJdbcSession())
      } yield Done,
      30.seconds)
    if (createBtcTransactionsTableFile.exists()) {
      Await.result(
        for {
          _ <- dropBtcTransactionsTable()
          _ <- SchemaUtils.applyScript(createUserTablesSql)
        } yield Done,
        30.seconds)
    }
    LoggerFactory
      .getLogger(this.getClass)
      .info("Created tables")
  }

  private def dropBtcTransactionsTable()(
    implicit system: ActorSystem[_]): Future[Done] = {
    SchemaUtils.applyScript("DROP TABLE IF EXISTS public.btc_transactions;")
  }

  private def createUserTablesSql: String = {
    val source = scala.io.Source.fromFile(createBtcTransactionsTableFile)
    val contents = source.mkString
    source.close()
    contents
  }
}
