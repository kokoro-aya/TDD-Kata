package org.example.database

import org.example.payload.Action
import java.sql.Timestamp

data class EntryLog(val action: Action, val snapshot: Pair<ULong, ULong?>, val timestamp: Timestamp = Timestamp(System.currentTimeMillis()))