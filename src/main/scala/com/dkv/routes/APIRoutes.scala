package com.dkv.routes

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ as, concat, entity, onSuccess, pathEnd, pathPrefix, rejectEmptyResponse }
import akka.http.scaladsl.server.{ PathMatchers, Route }
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout

import scala.concurrent.duration._
import com.dkv.JsonSupport
import com.dkv.model.{ DistributedKV, KeyValue }
import com.dkv.service.KeyValProcessor._

import scala.concurrent.Future
import akka.pattern.ask
import com.dkv.service.KeyValProcessor

//#user-routes-class
trait APIRoutes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[APIRoutes])

  // other dependencies that APIRoutes use
  def keyValProcessor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete
  lazy val userRoutes: Route =
    pathPrefix("dkv") {
      concat(
        //#users-get-delete
        pathEnd {
          post {
            entity(as[KeyValue]) { kv =>
              val keyAdded: Future[ActionPerformed] =
                (keyValProcessor ? CreateKeyValue(kv)).mapTo[ActionPerformed]
              onSuccess(keyAdded) { performed =>
                log.info("Add key {}: {}", kv.key, performed.description)
                complete((StatusCodes.Created, performed))
              }
            }
          }
        },
        path("sync") {
          post {
            entity(as[DistributedKV]) { dkvList =>
              val keyAdded: Future[ActionPerformed] =
                (keyValProcessor ? KeyValProcessor.SyncData(dkvList)).mapTo[ActionPerformed]
              onSuccess(keyAdded) { performed =>
                complete((StatusCodes.Created, performed))
              }
            }
          }
        },

        //#users-get-post
        //#users-get-delete
        path(PathMatchers.Segment) { key =>
          concat(
            get {
              //#retrieve-user-info
              val possibleKV: Future[Option[KeyValue]] =
                (keyValProcessor ? GetValue(key)).mapTo[Option[KeyValue]]
              onSuccess(possibleKV) {
                case Some(kv) => complete((StatusCodes.OK, kv))
                case _ => complete(StatusCodes.NotFound, KeyNotFound(s"Key $key does not exist!"))
              }
            },
            delete {
              //#users-delete-logic
              val userDeleted: Future[ActionPerformed] =
                (keyValProcessor ? DeleteKey(key)).mapTo[ActionPerformed]
              onSuccess(userDeleted) { performed =>
                log.info("Deleted key {}] {}", key, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            }
          )
        }
      )
      //#users-get-delete
    }
  //#all-routes
}
