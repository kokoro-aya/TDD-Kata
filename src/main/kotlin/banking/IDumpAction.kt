package org.example.banking

interface IDumpAction {
  fun printStatements(id: ULong): String
}