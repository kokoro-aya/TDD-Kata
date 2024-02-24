import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.example.database.adapter.DataAdapter
import org.example.database.Database
import org.example.database.payload.Action
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

class TestTransactions {

  var database: Database = Database()

  var adapter: DataAdapter = DataAdapter(database)

  @BeforeEach
  fun resetDatabase() {
    database.reset()
  }

  @Test
  fun testCreateEntry() = runTest {
    val command = adapter.createEntry(2uL)
    assertEquals(command, "CREATE id=2")
  }

  @Test
  fun testReadEntry() = runTest {
    val command = adapter.readEntry(1uL)
    assertEquals(command, "READ id=1")
  }

  @Test
  fun testRemoveEntry() = runTest {
    val command = adapter.removeEntry(1uL)
    assertEquals(command, "REMOVE id=1")
  }

  @Test
  fun testMinusEntry() = runTest {
    val command = adapter.minusToEntry(1uL, 97uL)
    assertEquals(command, "MINUS 97 TO id=1")
  }

  @Test
  fun testAddEntry() = runTest {
    val command = adapter.addToEntry(7uL, 103uL)
    assertEquals(command, "ADD 103 TO id=7")
  }

  @Test
  fun testDumpEntry() = runTest {
    val command = adapter.dumpEntry(17uL)
    assertEquals(command, "DUMP id=17")
  }

  @Test
  fun testAtomicSuccess1() = runTest {
    /*
      BEGIN
      CREATE 2
      CREATE 1
      ADD 600 TO 2
      ADD 300 TO 1
      READ 2
      READ 1
      END
     */
    val res = adapter.processTransaction(listOf(
      Action.CREATE to (2uL to null),
      Action.CREATE to (1uL to null),
      Action.ADD to (2uL to 600uL),
      Action.ADD to (1uL to 300uL),
      Action.READ to (1uL to null),
      Action.READ to (2uL to null)
    ))

    assertEquals(res, listOf(2uL to 0uL, 1uL to 0uL, 1uL to 300uL, 2uL to 600uL))
  }

  @Test
  fun testAtomicSuccess2() = runTest {
    /*
     BEGIN
     CREATE 2
     END

     BEGIN
     ADD 100 TO 2
     READ 2
     END
    */

    adapter.processTransaction(listOf(
        Action.CREATE to (2uL to null)
      ))

    val res = adapter.processTransaction(listOf(
      Action.ADD to (2uL to 100uL),
      Action.READ to (2uL to null)
    ))

    assertEquals(res, listOf(2uL to 100uL))
  }

  @Test
  fun testAtomicFail1() = runTest {
    /*
      BEGIN
      CREATE 2
      MINUS 100 TO 2
      END
     */
    val res = adapter.processTransaction(listOf(
      Action.CREATE to (2uL to null),
      Action.MINUS to (2uL to 100uL)
    ))

    assertEquals(res, listOf())
    assertEquals(database.dump(), listOf())
  }

  @Test
  fun testAtomicFail2() = runTest {
    /*
      BEGIN
      CREATE 1
      CREATE 2
      END

      BEGIN
      MINUS 100 TO 2
      CREATE 3
      END

      BEGIN
      ADD 100 TO 3
      ADD 100 TO 2
      ADD 100 TO 1
      END
     */
    adapter.processTransaction(listOf(
      Action.CREATE to (1uL to null),
      Action.CREATE to (2uL to null)
    ))
    val res1 = adapter.processTransaction(listOf(
      Action.MINUS to (2uL to 100uL),
      Action.CREATE to (3uL to null)
    ))
    val res2 = adapter.processTransaction(listOf(
      Action.ADD to (3uL to 100uL),
      Action.ADD to (2uL to 100uL),
      Action.ADD to (1uL to 100uL)
    ))

    assertEquals(res1, listOf())
    assertEquals(res2, listOf())
    assertEquals(database.dump(), listOf(1uL to 0uL, 2uL to 0uL))
  }

  // Consistency, Isolation and Duration are not covered here but they should be considered

  @Test
  fun testConcurrentStatements() = runTest {
    // two coroutines, first one change, second instant read but it should be blocked till operation finished

    adapter.processTransaction(listOf(
      Action.CREATE to (1uL to null),
      Action.CREATE to (2uL to null),
      Action.ADD to (1uL to 500uL),
      Action.ADD to (2uL to 500uL),
    ))

    val batches = listOf(
      listOf(
        Action.MINUS to (1uL to 200uL),
        Action.ADD to (2uL to 100uL)
      ),
      listOf(
        Action.MINUS to (1uL to 100uL),
        Action.CREATE to (3uL to 100uL)
      ),
      listOf(
        Action.MINUS to (1uL to 100uL)
      )
    )

    batches.map {
      async { adapter.processTransaction(it) }
    }.awaitAll()

    val res = adapter.processTransaction(listOf(
      Action.READ to (1uL to null),
      Action.READ to (2uL to null)
    ))

    assertEquals(res, listOf(1uL to 100uL, 2uL to 600uL))
  }
}