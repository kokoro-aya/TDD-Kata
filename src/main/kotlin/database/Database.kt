package org.example.database

import kotlinx.coroutines.sync.Mutex

class Database {

  // ACID, also usage of mutex instead of @Synchronized to limit it in coroutine scope
  val mutex = Mutex()

  // If aim for lock-free, several solutions possible like CAS or Lamport,
  // but CAS has ABA issue and Lamport could be inefficient


  // To simplify
  val database: Map<ULong, ULong> = mutableMapOf()

  fun createEntry(): Unit =
    TODO("Not implemented")

  fun removeEntry(): Unit =
    TODO("Not implemented")

  fun addToEntry(): Unit =
    TODO("Not implemented")

  fun minusToEntry(): Unit =
    TODO("Not implemented")

  fun readEntry(): Unit =
    TODO("Not implemented")

  fun unreadEntry(): Unit =
    TODO("Not implemented")


  // undo log, simulate the transaction provided by ORM layer
  fun rollback(): Unit =
    TODO("Not implemented")

  fun processCommand(): Unit =
    TODO("Not implemented")

  fun processBatchOfCommands(): Unit =
    TODO("Not implemented")


  // Obviously a bad way, just for test purpose
  fun setLock(state: Boolean): Unit =
    if (state) {
      mutex.tryLock()
      Unit
    } else {
      mutex.unlock()
    }
}