package org.example.database

import org.example.payload.Table

enum class Action {
  CREATE, DELETE, ADD, MINUS, READ
}

class Database {

  // If aim for lock-free, several solutions possible like CAS or Lamport,
  // but CAS has ABA issue and Lamport could be inefficient

  // To simplify
  private val database: MutableMap<ULong, Entry> = mutableMapOf()

  private val logs: MutableList<EntryLog> = mutableListOf()

  fun createEntry(id: ULong): Pair<ULong, ULong> {
    Constraints.shouldNotContainEntry(id, database) {
      database[id] = Entry()
    }

    return id to database[id]!!.value
  }

  fun removeEntry(id: ULong): Unit {
    Constraints.shouldContainEntry(id, database) {
      database.remove(id)
    }
  }

  fun addToEntry(id: ULong, value: ULong): Unit {
    database[id]?.let { entry ->
      entry.value += value
    }
  }

  fun minusToEntry(id: ULong, value: ULong): Unit {
    database[id]?.let { entry ->
      Constraints.shouldBeZeroOrPositive(entry.value, value) {
        entry.value -= value
      }
    }
  }

  fun readEntry(id: ULong): Pair<ULong, ULong?> {
    return id to database[id]?.value
  }

  // undo log, simulate the transaction provided by ORM layer
  fun rollback(/* TODO */): Unit =
    TODO("Not implemented")


  // ADD
  // MINUS
  // READ
  // CREATE
  // REMOVE

  fun processCommand(command: String): Table =
    TODO("Not implemented")

  fun processBatchOfCommands(/* TODO */): Table =
    TODO("Not implemented")

  fun reset() {
    this.database.clear()
  }

  fun dump(): Table
    = this.database.entries.map { (k, v) -> k to v.value }.toList()


  /*
    BEGIN
    ADD 10 TO 1992
    MINUS 9 TO 37
    END
 */

  /*
      READ
      ID=10
      VALUE=300
   */
}