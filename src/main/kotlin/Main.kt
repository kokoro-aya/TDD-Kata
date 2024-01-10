package org.example

import org.example.database.Database

fun main() {
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

  database.processBatchOfCommands("""
      BEGIN
      MINUS 100 TO id=2
      CREATE id=3
      END
    """.trim())
  println(database.dump())

  database.processBatchOfCommands("""
      BEGIN
      ADD 100 TO id=2
      ADD 100 TO id=3
      ADD 100 TO id=1
      END
    """.trim())
  println(database.dump())
}