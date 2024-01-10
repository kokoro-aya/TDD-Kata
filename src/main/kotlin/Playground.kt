package org.example

import kotlinx.coroutines.delay
import org.example.banking.BankingCore
import org.example.database.Database

suspend fun main() {
  val database = Database()

  database.processBatchOfCommands("""
      BEGIN
      CREATE id=1
      ADD 100 TO id=1
      CREATE id=2
      ADD 50 TO id=2
      END
    """.trim())
  println(database.dump())

  delay(150)

  database.processBatchOfCommands("""
      BEGIN
      MINUS 100 TO id=2
      CREATE id=3
      END
    """.trim())
  println(database.dump())

  delay(200)

  database.processBatchOfCommands("""
      BEGIN
      ADD 100 TO id=2
      ADD 100 TO id=3
      END
    """.trim())
  println(database.dump())

  delay(150)

  database.processBatchOfCommands("""
    BEGIN
    ADD 100 TO id=1
    ADD 200 TO id=1
    END
    """.trim())

  delay(230)

  database.processBatchOfCommands("""
    BEGIN
    MINUS 300 TO id=1
    END
    """.trim())

  delay(100)

  database.processBatchOfCommands("""
    BEGIN
    ADD 300 TO id=1
    ADD 400 TO id=1
    END
    """.trim())

  delay(120)

  database.processBatchOfCommands("""
    BEGIN
    MINUS 300 TO id=1
    END
    """.trim())

  delay(100)

//  database.dump(1uL)
//    .forEach { (date, v) ->
//      print(date)
//      print("\t")
//      println(v)
//    }

  val core = BankingCore(database)

  core.printStatements(1uL).let { println(it) }
}