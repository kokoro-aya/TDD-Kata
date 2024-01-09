package org.example.database

import org.example.payload.Action
import org.example.payload.Table
import org.example.utils.parseStatement

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

  fun processCommand(command: String): Table {
    val parsedCommand = parseStatement(command)

    return when (parsedCommand.first) {
      Action.CREATE -> {
        listOf(createEntry(parsedCommand.second.first))
      }
      Action.DELETE -> {
        removeEntry(parsedCommand.second.first)
        listOf()
      }
      Action.ADD -> {
        addToEntry(parsedCommand.second.first, parsedCommand.second.second!!)
        listOf()
      }
      Action.MINUS -> {
        minusToEntry(parsedCommand.second.first, parsedCommand.second.second!!)
        listOf()
      }
      Action.READ -> {
        listOf(readEntry(parsedCommand.second.first))
      }
    }
  }

  fun processBatchOfCommands(batch: String): Table {
    val lines = batch.split("\n").filter { it.isNotEmpty() }.map { it.trim() }
    if (lines.first() == "BEGIN" && lines.last() == "END") {
      return lines.drop(1).dropLast(1).flatMap { command ->
        processCommand(command)
      }
    } else {
      println("Database: Illegal batch command format, ignoring...")
      return listOf()
    }
  }

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