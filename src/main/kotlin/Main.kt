package org.example

import org.example.banking.BankingCore
import org.example.client.Account
import org.example.database.Database

fun main() {

  var database: Database = Database()
  var bankingCore: BankingCore = BankingCore(database)


  val account = Account(bankingCore)
  val status = account.withdraw(0.0)
  println(status)

}