package org.example.adapter

import org.example.database.Database

class DataAdapter(private var database: Database) {

  // Intentionally add some "bad design" like ULong? to see how it's taken care in core

  fun processTransaction(): ULong? =
    TODO("Not implemented")

  fun createEntry(): ULong? =
    TODO("Not implemented")

  fun addToEntry(): ULong? =
    TODO("Not implemented")

  fun minusToEntry(): ULong? =
    TODO("Not implemented")

  fun readEntry(): ULong? =
    TODO("Not implemented")

  fun commit(): ULong? =
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