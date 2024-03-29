import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.example.database.Database
import org.example.database.EntryIdentifierAlreadyExistException
import org.example.database.EntryIdentifierNotFoundException
import org.example.database.NegativeValueException
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class TestDatabase {

  private var database: Database = Database()

  @BeforeEach
  fun resetDatabase() {
    database.reset()
  }

  @Test
  fun testResetDatabase() = runTest {
    assertTrue { database.dump().isEmpty() }

    database.createEntry(1u)
    database.addToEntry(1u, 200u)

    assertFalse { database.dump().isEmpty() }

    database.reset()

    assertTrue { database.dump().isEmpty() }
  }

  @Test
  fun testCreateExistingEntry() = runTest {
    database.createEntry(1u)
    assertFailsWith(EntryIdentifierAlreadyExistException::class) { database.createEntry(1u) }
  }

  @Test
  fun testReadNonExistingEntry() = runTest {
    assertEquals(database.readEntry(29u).second, null)
  }

  @Test
  fun testCreateAndReadEntry() = runTest {
    database.createEntry(0u)
    assertEquals(database.readEntry(0u).second, 0uL)
  }

  @Test
  fun testAddAndReadEntry() = runTest {
    val valueToAdd: ULong = 429u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    assertEquals(database.readEntry(0u).second, valueToAdd)
  }

  @Test
  fun testMinusFailureEntry() = runTest {
    val valueToDrop: ULong = 429u

    database.createEntry(0u)
    assertFailsWith(NegativeValueException::class) { database.minusToEntry(0u, valueToDrop) }
  }

  @Test
  fun testAddAndMinusAndReadSuccessEntry() = runTest {
    val valueToAdd: ULong = 99u
    val valueToDrop: ULong = 24u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    database.minusToEntry(0u, valueToDrop)
    assertEquals(database.readEntry(0u).second, valueToAdd - valueToDrop)
  }

  @Test
  fun testAddAndMinusAndReadFailureEntry() = runTest {
    val valueToAdd: ULong = 42u
    val valueToDrop: ULong = 92u

    database.createEntry(0u)
    database.addToEntry(0u, valueToAdd)
    assertFailsWith(NegativeValueException::class) { database.minusToEntry(0u, valueToDrop) }
  }

  @Test
  fun testDeleteEntry() = runTest {
    // ok
    database.createEntry(0u)
    assertTrue { database.dump().isNotEmpty() }
    database.removeEntry(0u)
    assertTrue { database.dump().isEmpty() }
  }

  @Test
  fun testDeleteNonExistingEntry() = runTest {
    assertFailsWith(EntryIdentifierNotFoundException::class) { database.removeEntry(0u) }
  }

  @Test
  fun testDumpUserLog() = runTest {
    database.createEntry(1u)
    database.addToEntry(1u, 499u)
    database.minusToEntry(1u, 200u)
    database.addToEntry(1u, 1u)

    assertEquals(database.dump(1u), listOf(1uL to 0uL, 1uL to 499uL, 1uL to 299uL, 1uL to 300uL))
  }

  @Test
  fun testProcessSingleEntry1() = runTest {
    assertEquals(database.processCommand("CREATE id=12"), listOf(12uL to 0uL))
  }

  @Test
  fun testProcessSingleEntry2() = runTest {
    // ok, some random command
    database.createEntry(12uL)
    assertEquals(database.processCommand("ADD 250 TO id=12"), listOf<Pair<ULong, ULong>>())

  }

  @Test
  fun testProcessSingleEntry3() = runTest {
    // ok, some random command
    database.createEntry(12uL)
    database.addToEntry(12uL, 500uL)
    assertEquals(database.processCommand("MINUS 250 TO id=12"), listOf<Pair<ULong, ULong>>())
  }

  @Test
  fun testProcessSingleEntry4() = runTest {
    database.createEntry(12uL)
    database.addToEntry(12uL, 500uL)
    assertEquals(database.processCommand("READ id=12"), listOf(12uL to 500uL))


  }

  // Atomicity

  @Test
  fun testProcessBatchEntry1() = runTest {
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
  fun testProcessBatchEntry2() = runTest {
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
  fun testProcessBatchEntry3() = runTest {
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

    val dump3 = database.dump()
    assertEquals(dump3, listOf(1uL to 100uL, 2uL to 50uL))
  }

  @Test
  fun testNoRacing() = runTest {
    // test accessing same field by several commands but same result
    // accessing user 1, 2, 3, 4 with different transactions in various coroutines
    // with overlaps -> line mutex should work to maintain consistency

    val warmUp = """
        BEGIN
        CREATE id=1
        CREATE id=2
        ADD 500 TO id=1
        ADD 500 TO id=2
        END
      """.trim()

    val batches = listOf(
      """
        BEGIN
        MINUS 200 TO id=1
        ADD 100 TO id=2
        END
      """,
      """
        BEGIN
        MINUS 100 TO id=1
        ADD 100 TO id=2
        END
      """,
      """
        BEGIN
        MINUS 100 TO id=1
        END
      """
    ).map { it.trim() }

    val read = """
      BEGIN
      READ id=1
      READ id=2
      END
    """.trim()

    (1 .. 20).forEach {
      database.processBatchOfCommands(warmUp)

      batches.shuffled().map {
        async {
          database.processBatchOfCommands(it)
        }
      }.awaitAll()

      val result = database.processBatchOfCommands(read)

      assertEquals(result, listOf(1uL to 100uL, 2uL to 700uL))

      database.reset()
    }
  }

  @Test
  fun testParallelism() = runTest {
    // test accessing database at same time
    // accessing user 1, 2, 3, 4 with different transactions in various coroutines
    // no overlap -> these transactions should be parallelized

    // This test is suspicious since its result is randomized,
    // sometimes the test will pass but sometimes not
    // also because if the lock was designed to lock the whole DB,
    // the test will also pass although in this case rollbacks will be triggered


    val batches = listOf(
      """
        BEGIN
        CREATE id=1
        ADD 500 TO id=1
        READ id=1
        END
      """,
      """
        BEGIN
        CREATE id=2
        ADD 900 TO id=2
        READ id=2
        END
      """,
      """
        BEGIN
        CREATE id=3
        ADD 1200 TO id=3
        READ id=3
        END
      """,
      """
        BEGIN
        CREATE id=4
        ADD 1700 TO id=4
        READ id=4
        END
      """
    ).map { it.trim() }

    var inOrder = false
    var outOfOrder = false

    (1 .. 50).forEach {

      val resList: MutableList<Pair<ULong, ULong?>> = mutableListOf()
      batches.shuffled().map { batch ->
        async {
          val res = database.processBatchOfCommands(batch)
          resList.addAll(res)
        }
      }.awaitAll()
      // launch { ... } }.forEach { it.join() } will fail

      database.reset()

      // Only test if we have collected enough table results (4x2 rows)
      if (resList.size == 8) {

        // Both in-order and out-order should exist at least once in multiple simulations
        if (resList.map { it.first } == listOf(1uL, 1uL, 2uL, 2uL, 3uL, 3uL, 4uL, 4uL)) {
          inOrder = true
        } else {
          outOfOrder = true
        }
      }

    }

    assertTrue(inOrder)
    assertTrue(outOfOrder)
  }



}