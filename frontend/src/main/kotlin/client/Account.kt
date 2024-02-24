package org.example.client

import org.example.banking.BankingCore
import java.util.*

class Account : IAccount {

  // kata interface

  val bank: BankingCore
  val id: ULong

  constructor(bank: BankingCore) {
    this.bank = bank
    this.id = bank.create()
  }

  override fun deposit(value: Double): Unit {
    bank.deposit(id, value)
  }

  override fun withdraw(value: Double): Unit {
    bank.withdraw(id, value)
  }

  override fun printStatement(): String {
    return bank.printStatements(id)
  }
}