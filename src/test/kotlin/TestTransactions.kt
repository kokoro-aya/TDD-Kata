import org.example.adapter.DataAdapter
import org.example.database.Database
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.fail

class TestTransactions {

  var database: Database = Database()

  var adapter: DataAdapter = DataAdapter(database)

  @Test
  fun testReadNonExistingEntry() {
    // throw
    fail("Not implemented yet")
  }

  @Test
  fun testCreateAndReadEntry() {
    // 0
    fail("Not implemented yet")
  }

  @Test
  fun testAddAndReadEntry() {
    // 42
    fail("Not implemented yet")
  }

  @Test
  fun testMinusAndReadEntry() {
    // -97
    fail("Not implemented yet")
  }

  @Test
  fun testAddAndMinusAndReadEntry() {
    // 42
    // -92
    fail("Not implemented yet")
  }

  @Test
  fun testAtomicSuccess1() {
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
    fail("Not implemented yet")
  }

  @Test
  fun testAtomicSuccess2() {
    /*
      BEGIN
      CREATE 2
      END

      BEGIN
      ADD 100 TO 2
      READ 2
      END
     */
    fail("Not implemented yet")
  }

  @Test
  fun testAtomicFail1() {
    /*
      BEGIN
      CREATE 2
      MINUS 100 TO 2
      END
     */
    fail("Not implemented yet")
  }

  @Test
  fun testAtomicFail2() {
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
    fail("Not implemented yet")
  }

  // Consistency, Isolation and Duration are not covered here but they should be considered

  @Test
  fun testConcurrentStatements() {
    // two coroutines, first one change, second instant read but it should be blocked till operation finished

    fail("Not implemented yet")
  }
}