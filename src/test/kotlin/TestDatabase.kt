import org.example.database.Database
import kotlin.test.Test
import kotlin.test.fail

class TestDatabase {

  var database: Database = Database()

  @Test
  fun testReadNonExistingEntry() {
    // throw
    fail("Not implemented yet")
  }

  @Test
  fun testCreateExistingEntry() {
    // throw
    fail("Not implemented yet")
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

  // This testing case is quite strange, to review
  @Test
  fun testNoRacing() {
    database.setLock(true)

    // timeout
  }


}