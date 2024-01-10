import org.example.banking.BankingCore
import org.example.database.Database
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class TestBankingCore {

  // Unit tests for banking core

  var database: Database = Database()
  var bankingCore: BankingCore = BankingCore(database)

  @BeforeTest
  fun setup() {
    database.reset()
  }

  @Test
  fun testCreateNewUser() {
    val id = bankingCore.create()

    assertEquals(database.dump(), listOf(0uL to 0uL))
  }

  @Test
  fun testWithdrawNothingSuccess() {
    val id = bankingCore.create()
    bankingCore.withdraw(id, 0.0)

    assertEquals(database.dump(), listOf(0uL to 0uL))
  }

  @Test
  fun testDepositSuccess() {
    var id = bankingCore.create()
    bankingCore.deposit(id, 500.0)

    id = bankingCore.create()
    bankingCore.deposit(id, 99.0)

    assertEquals(database.dump(), listOf(0uL to 500_000_00uL, 1uL to 99_000_00uL))
  }

  @Test
  fun testDepositFailNoUser() {
    bankingCore.deposit(2uL, 500.0)
    assertEquals(database.dump(), listOf())
  }

  @Test
  fun testWithdrawFailNoUser() {
    bankingCore.withdraw(2uL, 500.0)
    assertEquals(database.dump(), listOf())
  }

  @Test
  fun testWithdrawFailInsufficientFund() {
    val id = bankingCore.create()
    bankingCore.deposit(id, 200.0)
    bankingCore.withdraw(id, 500.0)
    // rolled back
    assertEquals(database.dump(), listOf(0uL to 200_000_00uL))
  }

  @Test
  fun testDepositThenWithdrawSuccess1() {
    val id = bankingCore.create()
    bankingCore.deposit(id, 900.0)
    bankingCore.withdraw(id, 500.0)
    assertEquals(database.dump(), listOf(0uL to 400_000_00uL))
  }

  @Test
  fun testDepositThenWithdrawSuccess2() {
    val id = bankingCore.create()
    bankingCore.deposit(id, 900.0)
    bankingCore.withdraw(id, 250.0)
    assertEquals(database.dump(), listOf(0uL to 650_000_00uL))
  }

  @Test
  fun testPrintStatementShouldBe() {
    // ok, could be kata example
    val id = bankingCore.create()
    bankingCore.deposit(id, 500.0)
    bankingCore.withdraw(id, 100.0)
    val dump = bankingCore.printStatements(id)

    val date = SimpleDateFormat("dd.MM.yyyy").format(Date(System.currentTimeMillis()))

    assertEquals(dump, """
      Date           Amount   Balance
      -------------------------------
      $date	    500.0	    500.0
      $date	   -100.0	    400.0
      """.trimIndent())
  }
}