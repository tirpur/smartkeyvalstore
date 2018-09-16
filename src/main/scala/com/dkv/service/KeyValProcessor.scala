package com.dkv.service

//#user-registry-actor
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.dkv.model.{ DistributedKV, KeyValue }
import com.dkv.store.KeyValueStore

object KeyValProcessor {
  final case class ActionPerformed(description: String)
  final case class KeyNotFound(error: String)
  final case class InvalidOperation(reason: String)
  final case class CreateKeyValue(keyValue: KeyValue)
  final case class GetValue(key: String)
  final case class DeleteKey(key: String)
  final case class SyncData(dkvList: DistributedKV)
  final val ADD = "ADD"
  final val REMOVE = "DEL"
  final val SYNC_SUCCESS = "Data synced successfully!"
  final val SYNC_FAIL = "Syncing failed. Unknown Action: "

  def props: Props = Props[KeyValProcessor]
}

class KeyValProcessor(kvStore: KeyValueStore, syncActor: ActorRef) extends Actor with ActorLogging {
  import KeyValProcessor._

  def receive: Receive = {
    case CreateKeyValue(kv) =>
      kvStore.addKV(kv.key, kv.value)
      syncActor ! CreateKeyValue(kv)
      sender() ! ActionPerformed(s"Key ${kv.key} and value ${kv.value} is added.")
    case GetValue(key) =>
      kvStore.getValue(key) match {
        case Some(value) => sender() ! Some(KeyValue(key, value))
        case _ => sender() ! None
      }
    case DeleteKey(key) =>
      kvStore.remKey(key)
      syncActor ! DeleteKey(key)
      sender() ! ActionPerformed(s"Key $key is deleted.")
    case SyncData(dkv) =>
      log.info("Syncing data request came.. dkv=" + dkv)
      dkv.keysToRemove.foreach(key => kvStore.remKey(key))
      dkv.kvToAdd.foreach(kv => kvStore.addKV(kv.key, kv.value))
      sender() ! ActionPerformed("Data synced from peer.")
    case _ => sender() ! InvalidOperation("Not a valid operation!")
  }
}
//#user-registry-actor