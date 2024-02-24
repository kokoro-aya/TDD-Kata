import org.example.database.payload.Action
import org.example.database.utils.encodeResult
import org.example.database.utils.encodeStatement
import org.example.database.utils.parseResult
import org.example.database.utils.parseStatement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestParsingUtils {

  @Test
  fun testParsingStatementCREATE() {
    assertTrue { parseStatement("CREATE id=25") == Action.CREATE to (25uL to null) }
  }

  @Test
  fun testParsingStatementADD() {
    assertTrue { parseStatement("ADD 495 TO id=25") == Action.ADD to (25uL to 495uL) }
  }

  @Test
  fun testParsingStatementMINUS() {
    assertTrue { parseStatement("MINUS 167 TO id=25") == Action.MINUS to (25uL to 167uL) }
  }

  @Test
  fun testParsingStatementDELETE() {
    assertTrue { parseStatement("DELETE id=26") == Action.DELETE to (26uL to null) }
  }

  @Test
  fun testParsingStatementREAD() {
    assertTrue { parseStatement("READ id=27") == Action.READ to (27uL to null) }
  }

  @Test
  fun testParsingStatementDUMP() {
    assertEquals(parseStatement("DUMP id=2"), Action.DUMP to (2uL to null))
  }

  @Test
  fun testEncodingStatementCREATE() {
    assertTrue { encodeStatement(Action.CREATE to (25uL to null)) == "CREATE id=25" }
  }

  @Test
  fun testEncodingStatementADD() {
    assertTrue { encodeStatement(Action.ADD to (25uL to 495uL)) == "ADD 495 TO id=25" }
  }

  @Test
  fun testEncodingStatementMINUS() {
    assertTrue { encodeStatement(Action.MINUS to (25uL to 167uL)) == "MINUS 167 TO id=25" }
  }

  @Test
  fun testEncodingStatementDELETE() {
    assertTrue { encodeStatement(Action.DELETE to (26uL to null)) == "DELETE id=26" }
  }

  @Test
  fun testEncodingStatementREAD() {
    assertTrue { encodeStatement(Action.READ to (27uL to null)) == "READ id=27" }
  }

  @Test
  fun testEncodingStatementDUMP() {
    assertEquals(encodeStatement(Action.DUMP to (4uL to null)), "DUMP id=4")
  }

  @Test
  fun testParsingResult1() {
    assertTrue { parseResult("id=12: 59") == 12uL to 59uL }
  }

  @Test
  fun testParsingResult2() {
    assertTrue { parseResult("id=12: missing") == 12uL to null }
  }

  @Test
  fun testEncodeResult1() {
    assertTrue { encodeResult(12uL to null) == "id=12: missing" }
  }

  @Test
  fun testEncodeResult2() {
    assertTrue { encodeResult(12uL to 59uL) == "id=12: 59" }

  }
}