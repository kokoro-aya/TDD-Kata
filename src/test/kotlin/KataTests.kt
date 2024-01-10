import org.example.banking.BankingCore
import org.example.client.Account
import org.example.database.Database
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals

class KataTests {

  @Test
  fun test() {
    val database = Database()
    val bank = BankingCore(database)
    val app = Account(bank)

    app.deposit(500.0)
    app.withdraw(100.0)

    val actual = app.printStatement()

    val date = SimpleDateFormat("dd.MM.yyyy").format(Date(System.currentTimeMillis()))

    // Seems we cannot move back to 2016 easily only depending on java.util.Date

    val expected =
      """
        Date           Amount   Balance
        -------------------------------
        $date	    500.0	    500.0
        $date	   -100.0	    400.0
      """.trimIndent()

    assertEquals(actual, expected)

  }
}