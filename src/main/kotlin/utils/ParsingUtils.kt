package org.example.utils

import org.example.payload.Action

class MissingEncodingField(private val position: Int, private val action: Action) : Exception() {
  override val message: String
    get() = "While encoding $action, position $position is missing"
}

class MissingParsingField(private val position: Int, private val action: Action) : Exception() {
  override val message: String
    get() = "While parsing $action, position $position is missing"
}

fun encodeStatement(statement: Pair<Action, Pair<ULong, ULong?>>): String =
  when (statement.first) {
    Action.CREATE -> "CREATE id=${statement.second.first}"
    Action.DELETE -> "DELETE id=${statement.second.first}"
    Action.ADD -> {
      if (statement.second.second == null) {
        throw MissingEncodingField(2, Action.ADD)
      } else {
        "ADD ${statement.second.second} TO id=${statement.second.first}"
      }
    }
    Action.MINUS ->  {
      if (statement.second.second == null) {
        throw MissingEncodingField(2, Action.MINUS)
      } else {
        "MINUS ${statement.second.second} TO id=${statement.second.first}"
      }
    }
    Action.READ -> "READ id=${statement.second.first}"
  }

fun parseStatement(statement: String): Pair<Action, Pair<ULong, ULong?>> {
  val tokens = statement.split(" ").filter { it.isNotEmpty() }

  when (tokens.first()) {
    "CREATE" -> {
      if (tokens[1].startsWith("id=")) {
        val id = tokens[1].drop(3).toULong()
        return Action.CREATE to (id to null)
      }
    }
    "ADD" -> {
      if (tokens[2] == "TO" && tokens[3].startsWith("id=")) {
        val value = tokens[1].toULong()
        val id = tokens[3].drop(3).toULong()
        return Action.ADD to (id to value)
      }
    }
    "MINUS" -> {
      if (tokens[2] == "TO" && tokens[3].startsWith("id=")) {
        val value = tokens[1].toULong()
        val id = tokens[3].drop(3).toULong()
        return Action.MINUS to (id to value)
      }
    }
    "DELETE" -> {
      if (tokens[1].startsWith("id=")) {
        val id = tokens[1].drop(3).toULong()
        return Action.DELETE to (id to null)
      }
    }
    "READ" -> {
      if (tokens[1].startsWith("id=")) {
        val id = tokens[1].drop(3).toULong()
        return Action.READ to (id to null)
      }
    }
    else -> throw IllegalStateException("Unknown token")
  }

  throw IllegalStateException("This point should not be reached")
}

fun encodeResult(result: Pair<ULong, ULong?>): String =
  if (result.second == null) {
    "id=${result.first}: missing"
  } else {
    "id=${result.first}: ${result.second}"
  }

fun parseResult(result: String): Pair<ULong, ULong?> {
  val tokens = result.split(" ").filter { it.isNotEmpty() }

  if (tokens.first().startsWith("id=")) {
    if (tokens[1] == "missing") {
      return tokens.first().drop(3).dropLast(1).toULong() to null
    } else {
      val id = tokens.first().drop(3).dropLast(1).toULong()
      val value = tokens[1].toULong()

      return id to value
    }
  }
  throw IllegalStateException("Wrongly formatted result")
}