package com.dkv

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dkv.model.{ DistributedKV, KeyValue }
import com.dkv.service.KeyValProcessor.{ ActionPerformed, InvalidOperation, KeyNotFound }
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val kvJsonFormat = jsonFormat2(KeyValue)
  implicit val dkvJsonFormat = jsonFormat2(DistributedKV)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
  implicit val invalidOpsJsonFormat = jsonFormat1(InvalidOperation)
  implicit val keyNotFoundJsonFormat = jsonFormat1(KeyNotFound)

}
