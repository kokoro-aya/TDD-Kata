import org.example.banking.BankingCore
import org.example.client.Account
import org.example.database.Database
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.*

class TestAccountClient {

  // Unit tests for account

  var database: Database = Database()
  var bankingCore: BankingCore = BankingCore(database)

  @BeforeTest
  fun setup() {
    database.reset()
    bankingCore = BankingCore(database)
  }

  @Test
  fun testWithdrawNothingSuccess() {
    val account = Account(bankingCore)
    account.withdraw(0.0)
    assertEquals(database.dump(), listOf(0uL to 0uL))
  }

  @Test
  fun testDepositSuccess() {
    val account = Account(bankingCore)
    account.deposit(500.0)
    assertEquals(database.dump(), listOf(0uL to 500_000_00uL))
  }

  @Test
  fun testWithdrawFailInsufficientFund() {
    val account = Account(bankingCore)
    account.withdraw(200.0)
    assertEquals(database.dump(), listOf())
  }

  @Test
  fun testDepositThenWithdrawSuccess1() {
    val account = Account(bankingCore)
    account.deposit(1000.0)
    account.withdraw(500.0)
    assertEquals(database.dump(), listOf(0uL to 500_000_00uL))
  }

  @Test
  fun testDepositThenWithdrawSuccess2() {
    val account = Account(bankingCore)
    account.deposit(250.0)
    account.deposit(400.0)
    account.withdraw(500.0)
    assertEquals(database.dump(), listOf(0uL to 150_000_00uL))
  }

  @Test
  fun testDepositThenWithdrawFail() {
    val account = Account(bankingCore)
    account.deposit(250.0)
    account.deposit(200.0)
    account.withdraw(500.0)
    assertEquals(database.dump(), listOf())
  }

  @Test
  fun testPrintStatementShouldBe() {
    val account = Account(bankingCore)
    account.deposit(250.0)
    account.deposit(400.0)
    account.withdraw(500.0)
    val result = account.printStatement()

    val date = SimpleDateFormat("dd.MM.yyyy").format(Date(System.currentTimeMillis()))
    
    assertEquals(result, """
      Date           Amount   Balance
      -------------------------------
      $date	    250.0	    250.0
      $date	    400.0	    650.0
      $date	   -500.0	    150.0
      """.trimIndent())
  }
}