import org.example.banking.BankingCore
import org.example.client.Account
import org.example.database.Database
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IntegrationTests {

  @Test
  fun integrationTest() {
    val database = Database()
    val bank = BankingCore(database)
    val app1 = Account(bank)
    val app2 = Account(bank)

    app1.deposit(500.0)
    app2.deposit(200.0)
    app1.withdraw(600.0) // Rolled back
    app2.deposit(100.0)
    app1.withdraw(100.0)

    val date = SimpleDateFormat("dd.MM.yyyy").format(Date(System.currentTimeMillis()))

    val actual1 = app1.printStatement()

    val expected1 =
      """
        Date           Amount   Balance
        -------------------------------
        $date	    500.0	    500.0
        $date	      0.0	    500.0
        $date	   -100.0	    400.0
      """.trimIndent()

    val actual2 = app2.printStatement()

    val expected2 =
      """
        Date           Amount   Balance
        -------------------------------
        $date	      0.0	      0.0
        $date	    200.0	    200.0
        $date	    100.0	    300.0
      """.trimIndent()

    assertEquals(actual1, expected1)
    assertEquals(actual2, expected2)
  }

}