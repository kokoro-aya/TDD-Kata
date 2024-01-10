package org.example.database

class NegativeValueException(private val x: ULong, private val y: ULong) : Exception() {
  override val message: String
    get() = "Database encountered an error where subtraction of $x by $y gets negative result"
}

class EntryIdentifierNotFoundException(private val id: ULong) : Exception() {
  override val message: String
    get() = "Entry with identifier $id not found in database"
}

class EntryIdentifierAlreadyExistException(private val id: ULong) : Exception() {
  override val message: String
    get() = "Entry with identifier $id is already present in database"
}


object Constraints {

  /**
   * This contract checks if a subtraction is valid in terms of a banking system
   */
  suspend fun shouldBeZeroOrPositive(x: ULong, y: ULong, cont: suspend () -> Unit) {
    if (x > y) {
      cont()
    } else {
      throw NegativeValueException(x, y)
    }
  }

  /**
   * This contract checks if an entry of given id is present in the database
   */
  suspend fun shouldContainEntry(id: ULong, dict: MutableMap<ULong, Entry>, cont: suspend (ULong) -> Unit) {
    if (dict.containsKey(id)) {
      cont(id)
    } else {
      throw EntryIdentifierNotFoundException(id)
    }
  }

  /**
   * This contract checks if an entry of given id is not present in the database
   */
  suspend fun shouldNotContainEntry(id: ULong, dict: MutableMap<ULong, Entry>, cont: suspend (ULong) -> Unit) {
    if (dict.containsKey(id)) {
      throw EntryIdentifierAlreadyExistException(id)
    } else {
      cont(id)
    }
  }

  fun checkEntryIsPresent(id: ULong, dict: MutableMap<ULong, Entry>) {
    if (!dict.containsKey(id)) {
      throw EntryIdentifierNotFoundException(id)
    }
  }
}