import org.example.database.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class TestDatabase {

  var database: Database = Database()

  @BeforeEach
  fun resetDatabase() {
    database.reset()
  }

  @Test
  fun testResetDatabase() {
    assertTrue { database.dump().isEmpty() }

    database.createEntry(1u)
    database.addToEntry(1u, 200u)

    assertFalse { database.dump().isEmpty() }

    database.reset()

    assertTrue { database.dump().isEmpty() }
  }

  @Test
  fun testCreateExistingEntry() {
    database.createEntry(1u)
    assertFailsWith(EntryIdentifierAlreadyExistException::class) { database.createEntry(1u) }
  }

  @Test
  fun testReadNonExistingEntry() {
    assertTrue { database.readEntry(29u).second == null }
  }

  @Test
  fun testCreateAndReadEntry() {
    database.createEntry(0u)
    assertTrue { database.readEntry(0u).second == 0uL }
  }

  @Test
  fun testAddAndReadEntry() {
    val valueToAdd: ULong = 429u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    assertTrue { database.readEntry(0u).second == valueToAdd }
  }

  @Test
  fun testMinusFailureEntry() {
    val valueToDrop: ULong = 429u

    database.createEntry(0u)
    assertFailsWith(NegativeValueException::class) { database.minusToEntry(0u, valueToDrop) }
  }

  @Test
  fun testAddAndMinusAndReadSuccessEntry() {
    val valueToAdd: ULong = 99u
    val valueToDrop: ULong = 24u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    database.minusToEntry(0u, valueToDrop)
    assertTrue { database.readEntry(0u).second == valueToAdd - valueToDrop }
  }

  @Test
  fun testAddAndMinusAndReadFailureEntry() {
    val valueToAdd: ULong = 42u
    val valueToDrop: ULong = 92u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    assertFailsWith(NegativeValueException::class) { database.minusToEntry(0u, valueToDrop) }
  }

  @Test
  fun testDeleteEntry() {
    // ok
    database.createEntry(0u)
    assertTrue { database.dump().isNotEmpty() }
    database.removeEntry(0u)
    assertTrue { database.dump().isEmpty() }
  }

  @Test
  fun testDeleteNonExistingEntry() {
    assertFailsWith(EntryIdentifierNotFoundException::class) { database.removeEntry(0u) }
  }

  @Test
  fun testProcessSingleEntry1() {
    assertTrue { database.processCommand("CREATE id=12") == listOf(12uL to 0uL) }
  }

  @Test
  fun testProcessSingleEntry2() {
    // ok, some random command
    assertTrue { database.processCommand("ADD 250 TO id=12") == listOf<Pair<ULong, ULong>>() }

  }

  @Test
  fun testProcessSingleEntry3() {
    // ok, some random command
    assertTrue { database.processCommand("MINUS 250 TO id=12") == listOf<Pair<ULong, ULong>>() }
  }

  @Test
  fun testProcessSingleEntry4() {
    database.createEntry(12uL)
    database.addToEntry(12uL, 500uL)
    assertTrue { database.processCommand("READ id=12") == listOf(12uL to 500uL) }


  }

  // Atomicity

  @Test
  fun testProcessBatchEntry1() {
    // ok, some random batch commands
    fail("Not implemented yet")

  }

  @Test
  fun testProcessBatchEntry2() {
    // ok, some random batch commands
    fail("Not implemented yet")

  }

  @Test
  fun testProcessBatchEntry3() {
    // throw, some random batch commands with errors inside
    fail("Not implemented yet")

  }

  @Test
  fun testNoRacing() {
    // test accessing same field by several commands but same result
    fail("Not implemented yet")
  }

  @Test
  fun testParallelism() {
    // test accessing database at same time
    fail("Not implemented yet")
  }


}