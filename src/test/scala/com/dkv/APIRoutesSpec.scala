package com.dkv

//#user-routes-spec
//#test-top
import akka.actor.{ ActorRef, Props }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dkv.model.KeyValue
import com.dkv.routes.APIRoutes
import com.dkv.service.KeyValProcessor
import com.dkv.store.KeyValueStore
import com.mm.crs.service.DataSynchronizer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

//#set-up
class APIRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with APIRoutes {
  //#test-top

  // Here we need to implement all the abstract members of APIRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes, 
  // but we could "mock" it by implementing it in-place or by using a TestProbe() 

  val dictStore = new KeyValueStore
  val dataSynchronizer: ActorRef = system.actorOf(Props(new DataSynchronizer), "dataSyncActor")
  override val keyValProcessor: ActorRef = system.actorOf(Props(new KeyValProcessor(dictStore, dataSynchronizer)), "keyValueProcessorActor")

  lazy val routes = userRoutes

  //#set-up

  //#actual-test
  "APIRoutes" should {
    "return no key if no present (GET /dkv)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/dkv/notmykey")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"error":"Key notmykey does not exist!"}""")
      }
    }
    //#actual-test

    //#testing-post
    "be able to add keys (POST /dkv)" in {
      val kv = KeyValue("testkey", "testvalue")
      val userEntity = Marshal(kv).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/dkv").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"Key testkey and value testvalue is added."}""")
      }
    }
    //#testing-post

    //#actual-test
    "APIRoutes" should {
      "return valid key and value if present (GET /dkv)" in {
        // note that there's no need for the host part in the uri:
        val request = HttpRequest(uri = "/dkv/testkey")

        request ~> routes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===("""{"key":"testkey","value":"testvalue"}""")
        }
      }
    }
    //#actual-test

    "be able to remove keys (DELETE /dkv)" in {
      // user the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = "/dkv/testkey")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"Key testkey is deleted."}""")
      }
    }
    //#actual-test
  }
  //#actual-test

  //#set-up
}
//#set-up
//#user-routes-spec
