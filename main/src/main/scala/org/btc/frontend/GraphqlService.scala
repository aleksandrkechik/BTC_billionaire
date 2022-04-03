package org.btc.frontend

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import org.btc.frontend.graphql.Data
import org.btc.frontend.graphql.Schema
import org.slf4j.LoggerFactory
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.http.akka.circe.CirceHttpSupport
import sangria.slowlog.SlowLog

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class GraphqlService(endpointActor: ActorRef)(implicit executionContext: ExecutionContext) extends
  CirceHttpSupport {
  val log = LoggerFactory.getLogger(this.getClass.getName)
  implicit val timeout: Timeout = Timeout(2 seconds)

  import sangria.marshalling.circe._

//  override
  def route: Route = graphqlRoute

  val schema = Schema.schema

  val graphqlRoute: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
      path("graphql") {
        graphQLPlayground ~
            prepareGraphQLRequest {
              case Success(req) =>
                val middleware = if (tracing.isDefined) SlowLog.apolloTracing :: Nil else Nil
                val graphQLResponse = Executor.execute(
                  schema = schema,
                  queryAst = req.query,
                  Data.SecureContext(endpointActor),
                  variables = req.variables,
                  operationName = req.operationName,
                  middleware = middleware,
                ).map(OK -> _)
                  .recover {
                    case error: QueryAnalysisError => BadRequest -> error.resolveError
                    case error: ErrorWithResolver => InternalServerError -> error.resolveError
                  }
                complete(graphQLResponse)
              case Failure(preparationError) => complete(BadRequest, formatError(preparationError))
            }
      }
    }
}


