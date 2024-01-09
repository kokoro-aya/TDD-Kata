import org.example.database.Database
import org.example.database.EntryIdentifierAlreadyExistException
import org.example.database.EntryIdentifierNotFoundException
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
  fun testReadNonExistingEntry() {
    assertFailsWith(EntryIdentifierNotFoundException::class) { database.readEntry(29u) }
  }

  @Test
  fun testCreateExistingEntry() {
    database.createEntry(1u)
    assertFailsWith(EntryIdentifierAlreadyExistException::class) { database.createEntry(1u) }
  }

  @Test
  fun testCreateAndReadEntry() {
    // Create,
    // 0
    fail("Not implemented yet")
  }

  @Test
  fun testAddAndReadEntry() {
    // Create,
    // 42
    fail("Not implemented yet")
  }

  @Test
  fun testMinusAndReadEntry() {
    // Create,
    // -97
    fail("Not implemented yet")
  }

  @Test
  fun testAddAndMinusAndReadEntry() {
    // Create,
    // 42
    // -92
    fail("Not implemented yet")
  }

  @Test
  fun testDeleteEntry() {
    // ok
    fail("Not implemented yet")
  }

  @Test
  fun testDeleteNonExistingEntry() {
    // throw
    fail("Not implemented yet")
  }

  @Test
  fun testProcessSingleEntry1() {
    // ok, some random command
    fail("Not implemented yet")

  }

  @Test
  fun testProcessSingleEntry2() {
    // ok, some random command
    fail("Not implemented yet")

  }

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