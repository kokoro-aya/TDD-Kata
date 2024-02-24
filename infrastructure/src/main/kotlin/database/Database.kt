package org.example.database

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.database.payload.Action
import org.example.database.payload.Table
import org.example.database.utils.parseStatement
import java.sql.Timestamp
import java.time.ZoneOffset

class Database {

  // If aim for lock-free, several solutions possible like CAS or Lamport,
  // but CAS has ABA issue and Lamport could be inefficient

  private val globalMutex: Mutex = Mutex()

  // To simplify


  private val database: MutableMap<ULong, Entry> = mutableMapOf()

  private val logs: MutableList<EntryLog> = mutableListOf()

  suspend fun createEntry(id: ULong): Pair<ULong, ULong> {
    Constraints.shouldNotContainEntry(id, database) {
      val entry = Entry()
      val mutex = entry.mutex

      mutex.withLock {
        // Null since there is no previous value
        logs.add(EntryLog(Action.CREATE, id to 0uL))
        database[id] = Entry()
      }
    }.let {
      // To keep the execution order otherwise NPE may be thrown
      return id to database[id]!!.value
    }
  }

  suspend fun removeEntry(id: ULong): Unit {
    Constraints.shouldContainEntry(id, database) {
      val mutex = database[id]!!.mutex
      mutex.withLock {
        val lastValue = database[id]!!.value
        // Save last value to log for revert
        logs.add(EntryLog(Action.DELETE, id to lastValue))
        database.remove(id)
      }
    }
  }

  suspend fun addToEntry(id: ULong, value: ULong): Unit {
    database[id]?.let { entry ->
      val mutex = entry.mutex
      mutex.withLock {
        logs.add(EntryLog(Action.ADD, id to entry.value))
        entry.value += value
      }
    } ?: throw EntryIdentifierNotFoundException(id)
  }

  suspend fun minusToEntry(id: ULong, value: ULong): Unit {
    database[id]?.let { entry ->
      val mutex = entry.mutex
      mutex.withLock {
        logs.add(EntryLog(Action.MINUS, id to entry.value))
        Constraints.shouldBeZeroOrPositive(entry.value, value) {
          entry.value -= value
        }
      }
    } ?: throw EntryIdentifierNotFoundException(id)
  }

  suspend fun readEntry(id: ULong): Pair<ULong, ULong?> {
    return database[id]?.let { entry ->
      val mutex = entry.mutex
      mutex.withLock {
        val ret = id to entry.value
        ret
      }
    } ?: (id to null)
  }

  // Use global lock since the history is stored in log, which cannot be separated currently into line locks

  // Also this function use a view which is slightly different to other functions (Time, Amount) instead of (ID, Amount)
  // this idea could be troublesome
  suspend fun dump(id: ULong): List<Pair<ULong, ULong>> {
    return database[id]?.let { latest ->
      globalMutex.withLock {
        val ret = logs.drop(1).filter { entry -> entry.snapshot.first == id }
          .sortedBy { it.timestamp }
          .map { it.timestamp.toLocalDateTime().toEpochSecond(ZoneOffset.UTC).toULong() to it.snapshot.second!! }
        ret + listOf(Timestamp(System.currentTimeMillis()).toLocalDateTime().toEpochSecond(ZoneOffset.UTC).toULong() to latest.value)
      }
    } ?: listOf()
  }

  // undo log, simulate the transaction provided by ORM layer
  private suspend fun rollback(begin: Timestamp): Unit {
    println("Rollback happened")
    val eventsToRollback = logs.filter { it.timestamp >= begin }.sortedByDescending { it.timestamp }
    globalMutex.withLock {
      eventsToRollback.forEach {
        rollbackEvent(it)
      }
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
      Action.DUMP -> {

      }
    }
  }


  // ADD
  // MINUS
  // READ
  // CREATE
  // REMOVE

  suspend fun processCommand(command: String): Table {
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
      Action.DUMP -> {
        dump(parsedCommand.second.first)
      }
    }
  }

  private suspend fun executeTransaction(commands: List<String>): Table {
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

  suspend fun processBatchOfCommands(batch: String): Table {
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