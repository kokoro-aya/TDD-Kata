package org.example.client

interface IAccount {

  fun deposit(value: Double): Unit

  fun withdraw(value: Double): Unit

  fun printStatement(): String

}