package org.btc.frontend.graphql

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.directives.Credentials
import akka.pattern.ask
import akka.util.Timeout
import sangria.execution.HandledException
import sangria.marshalling.ResultMarshaller

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object Data {

//  class UserAuthRepo(authActor: ActorRef) {
//    implicit val timeout: Timeout = Timeout(1 second)
//
//    //    var tokens = Map.empty[String, UserAccountWithRolesWithPermissions]
//
///*
//    /** Gives back a token or sessionId or anything else that identifies the user session  */
//    def authenticate(userName: String, password: String): Option[String] =
//      if (userName == Edoque && password == "secret") {
//        val token = UUID.randomUUID().toString
//        tokens = tokens + (token → UserAccount("admin", "VIEW_PERMISSIONS" :: "EDIT_COLORS" :: "VIEW_COLORS" :: Nil))
//        Some(token)
//      } else if (userName == "john" && password == "apples") {
//        val token = UUID.randomUUID().toString
//        tokens = tokens + (token → UserAccount("john", "VIEW_COLORS" :: Nil))
//        Some(token)
//      } else None
//*/
//
//    /** Gives `UserAccount` object with his/her permissions */
//    def authorise(token: String): Option[LoggedInUser] = {
//      val credentials = Credentials(Some(OAuth2BearerToken(token))).asInstanceOf[Credentials.Provided]
//      val eventualMaybeUser: Future[Option[LoggedInUser]] = (authActor ? OAuthenticateUserRequest(credentials)).mapTo[Option[LoggedInUser]]
//      Await.result(eventualMaybeUser, timeout.duration)
//    }
////      tokens.get(token)
//  }
//
//  case class AuthenticationException(message: String) extends Exception(message)
//  case class AuthorisationException(message: String) extends Exception(message)

  case class SecureContext(endpointActor: ActorRef) {

  }
    /*def login(userName: String, password: String) = userRepo.authenticate(userName, password) getOrElse (
        throw new AuthenticationException("UserAccountName or password is incorrect"))
*/
//    def authorised[T](permissions: PermissionName*)(fn: LoggedInUser ⇒ T) =
//      token.flatMap(userRepo.authorise).fold(throw AuthorisationException("Invalid token")) { loggedInUser ⇒
//        if (permissions.forall(loggedInUser.user.permissions.contains)) fn(loggedInUser)
//        else throw AuthorisationException("You do not have permission to do this operation")
//      }
//
//    def ensurePermissions(permissions: List[PermissionName]): Unit =
//      token.flatMap(userRepo.authorise).fold(throw AuthorisationException("Invalid token")) { loggedInUser ⇒
//        if (!permissions.forall(loggedInUser.user.permissions))
//          throw AuthorisationException("You do not have permission to do this operation")
//      }
//
//    def user = token.flatMap(userRepo.authorise).fold(throw AuthorisationException("Invalid token"))(identity)
//  }
//
//  val errorHandler: PartialFunction[(ResultMarshaller, Throwable), HandledException] = {
//    case (m, AuthenticationException(message)) ⇒ HandledException(message)
//    case (m, AuthorisationException(message)) ⇒ HandledException(message)
//  }
}