import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
    assertEquals(database.readEntry(29u).second, null)
  }

  @Test
  fun testCreateAndReadEntry() {
    database.createEntry(0u)
    assertEquals(database.readEntry(0u).second, 0uL)
  }

  @Test
  fun testAddAndReadEntry() {
    val valueToAdd: ULong = 429u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    assertEquals(database.readEntry(0u).second, valueToAdd)
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
    assertEquals(database.readEntry(0u).second, valueToAdd - valueToDrop)
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
    assertEquals(database.processCommand("CREATE id=12"), listOf(12uL to 0uL))
  }

  @Test
  fun testProcessSingleEntry2() {
    // ok, some random command
    database.createEntry(12uL)
    assertEquals(database.processCommand("ADD 250 TO id=12"), listOf<Pair<ULong, ULong>>())

  }

  @Test
  fun testProcessSingleEntry3() {
    // ok, some random command
    database.createEntry(12uL)
    database.addToEntry(12uL, 500uL)
    assertEquals(database.processCommand("MINUS 250 TO id=12"), listOf<Pair<ULong, ULong>>())
  }

  @Test
  fun testProcessSingleEntry4() {
    database.createEntry(12uL)
    database.addToEntry(12uL, 500uL)
    assertEquals(database.processCommand("READ id=12"), listOf(12uL to 500uL))


  }

  // Atomicity

  @Test
  fun testProcessBatchEntry1() {
    // ok, some random batch commands
    /*
      Add a user of id 2, give it 100 credits and display his data
     */
    database.processBatchOfCommands("""
      BEGIN
      CREATE id=2
      END
    """.trim())
    assertEquals(database.dump().size, 1)

    database.processBatchOfCommands("""
      BEGIN
      ADD 100 TO id=2
      READ id=2
      END
    """.trim())
    assertEquals(database.dump(), listOf(2uL to 100uL))

  }

  @Test
  fun testProcessBatchEntry2() {
    // fail, some random batch commands

    /*
      Add a user with id 2, drop 100 credits whilst his balance is zero, no transaction should be performed.
      The account should not exist.
     */
    database.processBatchOfCommands("""
      BEGIN
      CREATE id=2
      MINUS 100 TO id=2
      END
    """.trim())
    assertTrue { database.dump().isEmpty() }
  }

  @Test
  fun testProcessBatchEntry3() {
    // throw, some random batch commands with errors inside
    /*
       First create two user with id 1 and 2, give them 100 and 50 credits

       Create user 3 when removing 100 credits from user 2

       Add 100 credits to user 1, 2 and 3

       As a result, second and third batch should be rolled back, only user 1 with 100 credit and 2 with 50 credit should left
     */

    database.processBatchOfCommands("""
      BEGIN
      CREATE id=1
      ADD 100 TO id=1
      CREATE id=2
      ADD 50 TO id=2
      END
    """.trim())

    val dump1 = database.dump()
    assertEquals(dump1, listOf(1uL to 100uL, 2uL to 50uL))

    database.processBatchOfCommands("""
      BEGIN
      MINUS 100 TO id=2
      CREATE id=3
      END
    """.trim())

    val dump2 = database.dump()
    assertEquals(dump2, listOf(1uL to 100uL, 2uL to 50uL))

    database.processBatchOfCommands("""
      BEGIN
      ADD 100 TO id=2
      ADD 100 TO id=3
      ADD 100 TO id=1
      END
    """.trim())
    assertEquals(database.dump(), listOf(1uL to 100uL, 2uL to 50uL))
  }

  val scope = MainScope()

  @Test
  fun testNoRacing() {
    // test accessing same field by several commands but same result
    // accessing user 1, 2, 3, 4 with different transactions in various coroutines
    // with overlaps -> line mutex should work to maintain consistency

    val batches = listOf(
      """
      """,
      """
      """,
      """
      """,
      """
      """
    ).map { it.trim() }



    scope.launch {

    }

  }

  @Test
  fun testParallelism() {
    // test accessing database at same time
    // accessing user 1, 2, 3, 4 with different transactions in various coroutines
    // no overlap -> these transactions should be parallelized
    fail("Not implemented yet")
  }


}