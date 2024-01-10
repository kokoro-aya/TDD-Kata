package org.example.adapter

import org.example.database.Database
import org.example.payload.Action
import org.example.payload.Table

class DataAdapter(private var database: Database) {

  private fun prepareStatements(statements: List<Pair<Action, Pair<ULong, ULong?>>>): String {
    return "BEGIN\n" + statements.map { stmt ->
      when (stmt.first) {
        Action.CREATE -> createEntry(stmt.second.first)
        Action.DELETE -> removeEntry(stmt.second.first)
        Action.ADD -> addToEntry(stmt.second.first, stmt.second.second!!)
        Action.MINUS -> minusToEntry(stmt.second.first, stmt.second.second!!)
        Action.READ -> readEntry(stmt.second.first)
        Action.DUMP -> dumpEntry(stmt.second.first)
      }
    }.joinToString("\n") + "\nEND"
  }

  private suspend fun commitToDatabase(statements: String): Table =
    database.processBatchOfCommands(statements)

  suspend fun processTransaction(statements: List<Pair<Action, Pair<ULong, ULong?>>>): Table {
    val preparedStatements = prepareStatements(statements)
    return commitToDatabase(preparedStatements)
  }

  fun createEntry(id: ULong): String =
    "CREATE id=$id"

  fun addToEntry(id: ULong, value: ULong): String =
    "ADD $value TO id=$id"

  fun minusToEntry(id: ULong, value: ULong): String =
    "MINUS $value TO id=$id"

  fun readEntry(id: ULong): String =
    "READ id=$id"

  fun removeEntry(id: ULong): String =
    "REMOVE id=$id"

  fun dumpEntry(id: ULong): String =
    "DUMP id=$id"
}