package org.example.adapter

import org.example.database.Database
import org.example.payload.Table

class DataAdapter(private var database: Database) {

  fun processTransaction(): Table =
    TODO("Not implemented")

  fun createEntry(id: ULong): Table =
    TODO("Not implemented")

  fun addToEntry(id: ULong, value: ULong): Table =
    TODO("Not implemented")

  fun minusToEntry(id: ULong, value: ULong): Table =
    TODO("Not implemented")

  fun readEntry(id: ULong): Table =
    TODO("Not implemented")

  fun commit(): Table =
    TODO("Not implemented")

  /*
      BEGIN
      ADD 10 TO 1992
      MINUS 9 TO 37
      END
   */

  /*
      READ
      ID=10
      VALUE=300
   */

}