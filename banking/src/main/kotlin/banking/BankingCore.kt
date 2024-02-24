package org.example.banking

import kotlinx.coroutines.*
import org.example.database.adapter.DataAdapter
import org.example.database.Database
import org.example.database.payload.Action
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class BankingCore : IDumpAction, IOperations {

  private val database: Database

  private val adapter: DataAdapter

  private var currUserCount = 0uL

  constructor(database: Database) {
    this.database = database
    this.adapter = DataAdapter(database)
  }

  private fun nextAvailableId(): ULong {
    return currUserCount ++
  }

  private fun convertDoubleToULong(amount: Double): ULong =
    (amount * 100_000L).toULong()

  private fun convertULongBackToDouble(internal: Long): Double =
    (internal.toDouble()) / 100_000f

  override fun create(): ULong {
    val res = runBlocking {
      async {
        adapter.processTransaction(listOf(Action.CREATE to (nextAvailableId() to null)))
      }.await()
    }
    return res[0].first
  }

  // check fund then decide

  override fun deposit(id: ULong, amount: Double): Unit {
    val amount = convertDoubleToULong(amount)

    val res = runBlocking {
      async {
        adapter.processTransaction(listOf(Action.ADD to (id to amount)))
      }.await()
    }
  }

  // check fund and account property
  // if present and enough fund -> withdraw
  // else -> do nothing
  override fun withdraw(id: ULong, amount: Double): Unit {
    val amount = convertDoubleToULong(amount)

    val res = runBlocking {
      async {
        adapter.processTransaction(listOf(Action.MINUS to (id to amount)))
      }.await()
    }
  }

  override fun printStatements(id: ULong): String {
    val table = runBlocking {
      async {
        adapter.processTransaction(listOf(Action.DUMP to (id to null)))
      }.await()
    }

    val heading =
        "Date           Amount   Balance\n" +
        "-------------------------------"

    val res = table.mapIndexed { i, (time, amount) ->
      if (i == 0) {
        ""
      } else {
        val formattedTime = SimpleDateFormat("dd.MM.yyyy").format(Date(1000 * time.toLong()))

        val changedAmountIntern = (amount!!).toLong() - (table[i - 1].second!!).toLong()

        val changedAmount = convertULongBackToDouble(changedAmountIntern)

        val balance = convertULongBackToDouble(amount.toLong())

        "$formattedTime\t${changedAmount.toString().padStart(9)}\t${balance.toString().padStart(9)}"
      }
    }.joinToString("\n")

    return heading + res
  }
}