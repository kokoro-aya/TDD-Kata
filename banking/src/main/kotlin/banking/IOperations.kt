package org.example.banking

interface IOperations {

  fun create(): ULong

  fun deposit(id: ULong, amount: Double): Unit

  fun withdraw(id: ULong, amount: Double): Unit
}