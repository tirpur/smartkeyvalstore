package com.dkv.model

case class DistributedKV(kvToAdd: List[KeyValue], keysToRemove: List[String]) extends Serializable
