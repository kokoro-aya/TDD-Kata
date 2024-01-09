package org.example.banking

class BankingCore {

  // cache
  val cache: MutableList<BankingDataEntry> = mutableListOf()

  // check fund then decide

  fun deposit(): Unit = TODO("Not implemented yet")

  // check fund and account property
  // if present and enough fund -> withdraw
  // else -> do nothing
  fun withdraw(): Unit = TODO("Not implemented yet")

  fun printStatement(): String = TODO("Not implemented yet")
}