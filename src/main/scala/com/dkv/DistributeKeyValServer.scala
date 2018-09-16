package com.dkv

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.dkv.routes.APIRoutes
import com.dkv.service.KeyValProcessor
import com.dkv.store.KeyValueStore
import com.mm.crs.service.DataSynchronizer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//#main-class
object DistributeKeyValServer extends App with APIRoutes {

  val appConf = ConfigFactory.load()
  private final val portConfig = "dkv.server.port"

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("DistributedKeyValueStoreSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val dictStore = new KeyValueStore
  val dataSynchronizer: ActorRef = system.actorOf(Props(new DataSynchronizer), "dataSyncActor")
  val keyValProcessor: ActorRef = system.actorOf(Props(new KeyValProcessor(dictStore, dataSynchronizer)), "keyValueProcessorActor")

  //#main-class
  // from the APIRoutes trait
  lazy val routes: Route = userRoutes
  //#main-class

  //#http-server
  Http().bindAndHandle(routes, "0.0.0.0", appConf.getInt(portConfig))

  println(s"Server online at http://localhost:" + appConf.getInt(portConfig))

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
