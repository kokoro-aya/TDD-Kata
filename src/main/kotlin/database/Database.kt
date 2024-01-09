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

  fun createEntry(id: ULong): Table {
    return listOf()
  }

  fun removeEntry(id: ULong): Unit {
    return Unit
  }

  fun addToEntry(id: ULong, value: ULong): Unit {
    return Unit
  }

  fun minusToEntry(id: ULong, value: ULong): Unit {
    return Unit
  }

  fun readEntry(id: ULong): Pair<ULong, ULong?> {
    throw IllegalStateException()
  }

  // undo log, simulate the transaction provided by ORM layer
  fun rollback(/* TODO */): Unit =
    TODO("Not implemented")

  fun processCommand(/* TODO */): Table =
    TODO("Not implemented")

  fun processBatchOfCommands(/* TODO */): Table =
    TODO("Not implemented")

  fun reset() {
    this.database.clear()
  }

  fun dump(): Table
    = this.database.entries.map { (k, v) -> k to v.value }.toList()
}