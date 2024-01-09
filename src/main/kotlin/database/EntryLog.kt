package org.example.database

import org.example.payload.Action
import java.util.*

data class EntryLog(val action: Action, val timestamp: Date, val snapshot: Pair<ULong, ULong>)