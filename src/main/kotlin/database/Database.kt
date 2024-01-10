package org.example.database

import kotlinx.coroutines.sync.Mutex
import org.example.payload.Action
import org.example.payload.Table
import org.example.utils.parseStatement
import java.sql.Timestamp

class Database {

  // If aim for lock-free, several solutions possible like CAS or Lamport,
  // but CAS has ABA issue and Lamport could be inefficient

  private val globalMutex: Mutex = Mutex()

  // To simplify


  private val database: MutableMap<ULong, Entry> = mutableMapOf()

  private val logs: MutableList<EntryLog> = mutableListOf()

  fun createEntry(id: ULong): Pair<ULong, ULong> {
    Constraints.shouldNotContainEntry(id, database) {
      // Null since there is no previous value
      logs.add(EntryLog(Action.CREATE, id to null))
      database[id] = Entry()
    }

    return id to database[id]!!.value
  }

  fun removeEntry(id: ULong): Unit {
    Constraints.shouldContainEntry(id, database) {
      val lastValue = database[id]!!.value
      // Save last value to log for revert
      logs.add(EntryLog(Action.DELETE, id to lastValue))
      database.remove(id)
    }
  }

  fun addToEntry(id: ULong, value: ULong): Unit {
    database[id]?.let { entry ->
      logs.add(EntryLog(Action.ADD, id to entry.value))
      entry.value += value
    } ?: throw EntryIdentifierNotFoundException(id)
  }

  fun minusToEntry(id: ULong, value: ULong): Unit {
    database[id]?.let { entry ->
      logs.add(EntryLog(Action.MINUS, id to entry.value))
      Constraints.shouldBeZeroOrPositive(entry.value, value) {
        entry.value -= value
      }
    } ?: throw EntryIdentifierNotFoundException(id)
  }

  fun readEntry(id: ULong): Pair<ULong, ULong?> {
    return id to database[id]?.value
  }

  // undo log, simulate the transaction provided by ORM layer
  fun rollback(begin: Timestamp): Unit {
    val eventsToRollback = logs.filter { it.timestamp >= begin }.sortedByDescending { it.timestamp }
    globalMutex.tryLock()
    eventsToRollback.forEach {
      rollbackEvent(it)
    }
  }

  private fun rollbackEvent(entry: EntryLog) {
    val id = entry.snapshot.first
    val lastValue = entry.snapshot.second

    when (entry.action) {
      Action.CREATE -> database.remove(id)
      Action.DELETE -> {
        if (lastValue != null) {
          val restoredEntry = Entry()
          restoredEntry.value = lastValue

          database[id] = Entry()
        }
      }
      Action.ADD -> {
        if (lastValue != null && database.containsKey(id)) {
          database[id]!!.value = lastValue
        }
      }
      Action.MINUS -> {
        if (lastValue != null && database.containsKey(id)) {
          database[id]!!.value = lastValue
        }
      }
      Action.READ -> {

      }
    }
  }


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

  private fun executeTransaction(commands: List<String>): Table {
    val startingTimestamp = Timestamp(System.currentTimeMillis())
    try {
      return commands.flatMap { command ->
        processCommand(command)
      }
    } catch (e: Exception) {
      println(e.message)
      rollback(startingTimestamp)
      return listOf()
    }
  }

  fun processBatchOfCommands(batch: String): Table {
    val lines = batch.split("\n").filter { it.isNotEmpty() }.map { it.trim() }
    if (lines.first() == "BEGIN" && lines.last() == "END") {
      val commands = lines.drop(1).dropLast(1)
      return executeTransaction(commands)
    } else {
      println("Database: Illegal batch command format, ignoring...")
      return listOf()
    }
  }

  fun reset() {
    this.database.clear()
  }

  fun dump(): Table
    = this.database.entries.map { (k, v) -> k to v.value }.sortedBy { it.first }.toList()


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