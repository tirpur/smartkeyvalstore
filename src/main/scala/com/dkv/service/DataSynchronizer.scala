package com.mm.crs.service

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Cancellable }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ Multipart, _ }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.dkv.JsonSupport
import com.dkv.model.{ DistributedKV, KeyValue }
import com.dkv.service.KeyValProcessor.{ ActionPerformed, CreateKeyValue, DeleteKey, GetValue }
import com.dkv.store.KeyValueStore
import com.typesafe.config.ConfigFactory
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.parsing.json.JSON
import scala.util.{ Success, Try }

object DataSynchronizer {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  private final val PollConfig = "CheckForDataSync"
  val appConf = ConfigFactory.load()
  val URLCONFIG = "dkv.peer.endpoints"
  val SYNC_URI = "/dkv/sync"
  val scheme = "http://"

}
class DataSynchronizer extends Actor with ActorLogging with JsonSupport {

  import DataSynchronizer._
  var unsyncAdditions = List[KeyValue]()
  var unsyncRemovals = List[String]()
  var urls = List[String]()

  var pollScheduler: Cancellable = null
  var lastPollRevision = 0l
  override def preStart(): Unit = {
    pollScheduler =
      system.scheduler.schedule(
        5 seconds,
        10 seconds,
        self,
        PollConfig
      )

    val str = Try(appConf.getString(URLCONFIG))
    val endPoints = str match {
      case Success(value) => value.split(",").toList
      case _ => List()
    }
    urls = endPoints.map(scheme + _ + SYNC_URI)
  }

  override def postStop(): Unit = {
    if (pollScheduler != null) pollScheduler.cancel()
  }

  def getDistributedKV: Option[DistributedKV] = {
    if (unsyncRemovals.nonEmpty || unsyncAdditions.nonEmpty) {
      Some(DistributedKV(unsyncAdditions, unsyncRemovals))
    } else None
  }

  val http = Http(system)

  def postApplication(app: DistributedKV, url: String): Unit = {
    log.info("Request being made to url: " + url + " with dkv=" + app)
    val req = Marshal(app).to[RequestEntity] flatMap { entity =>
      val request = HttpRequest(method = HttpMethods.POST, uri = url, entity = entity)
      http.singleRequest(request)
    }
    req.onComplete(x => log.info("Sync completed:" + x))
  }

  override def receive: Receive = {
    case PollConfig =>
      log.info(" PollConfig called: unsyncAdditions=" + unsyncAdditions + " unsync removal=" + unsyncRemovals)
      getDistributedKV match {
        case Some(dkv) if urls.nonEmpty => urls.foreach(url => postApplication(dkv, url))
        case _ => //do nothing
      }
      unsyncAdditions = List() //reset
      unsyncRemovals = List() //reset
    case CreateKeyValue(kv) =>
      log.info("Data has been added so add to unsync additions to update it's peers: " + kv)
      unsyncAdditions = kv :: unsyncAdditions
    case DeleteKey(key) => unsyncRemovals = unsyncRemovals.filter(k => !key.equals(k))
  }
}
