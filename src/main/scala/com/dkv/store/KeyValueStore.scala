package com.dkv.store

class KeyValueStore {
  private var store = Map[String, String]()
  def addKV(k: String, v: String): Unit = {
    store += (k -> v)
  }
  def getValue(k: String): Option[String] = store.get(k)

  def remKey(key: String): Unit = {
    if (exists(key))
      store = store.filter(tpl => !tpl._1.equals(key))
  }
  def exists(k: String) = store.contains(k)
}
