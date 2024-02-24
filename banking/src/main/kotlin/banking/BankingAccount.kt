package org.example.banking

data class BankingAccount(val id: ULong, var amount: ULong)

data class BankingDataEntry(val account: BankingAccount, var dirty: Boolean)